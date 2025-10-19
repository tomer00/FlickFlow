package com.tomer.myflix.player

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.MappingTrackSelector
import com.tomer.myflix.presentation.ui.models.TrackInfo

//region SUBS

@OptIn(UnstableApi::class)
fun getSubtitleRendererIndex(
    mappedTrackInfo: MappingTrackSelector.MappedTrackInfo,
    exoPlayer: ExoPlayer
): Int {
    for (rendererIndex in 0 until mappedTrackInfo.rendererCount) {
        if (exoPlayer.getRendererType(rendererIndex) == C.TRACK_TYPE_TEXT) {
            return rendererIndex
        }
    }
    return C.INDEX_UNSET
}

@OptIn(UnstableApi::class)
fun getSubtitleTrackInfo(
    mappedTrackInfo: MappingTrackSelector.MappedTrackInfo,
    exoPlayer: ExoPlayer
): List<TrackInfo> {
    val subtitleTrackInfoList = mutableListOf<TrackInfo>()
    for (rendererIndex in 0 until mappedTrackInfo.rendererCount) {
        if (mappedTrackInfo.getRendererType(rendererIndex) == C.TRACK_TYPE_TEXT) {
            val trackGroups = mappedTrackInfo.getTrackGroups(rendererIndex)
            for (groupIndex in 0 until trackGroups.length) {
                val trackGroup = trackGroups.get(groupIndex)
                for (trackIndex in 0 until trackGroup.length) {
                    val format = trackGroup.getFormat(trackIndex)
                    var language = ""
                    format.label?.let { language = it }
                    if (language.isEmpty())
                        language = getLanguageName(format.language)
                    val id = format.id ?: "s:$language"
                    val isSel = run {
                        val groups = exoPlayer.currentTracks
                        for (group in groups.groups) {
                            if (group.isSelected) {
                                for (i in 0 until group.length) {
                                    val formatG = group.getTrackFormat(i)
                                    if ((formatG.id ?: "s:${formatG.language ?: ""}") == id)
                                        return@run true
                                }
                            }
                        }
                        return@run false
                    }
                    if (language != "Unknown")
                        subtitleTrackInfoList.add(
                            TrackInfo(
                                groupIndex,
                                trackIndex,
                                language,
                                isSel
                            )
                        )
                }
            }
        }
    }
    return subtitleTrackInfoList
}

//endregion SUBS

//region AUDIO TRACKS
@OptIn(UnstableApi::class)
fun getAudioRendererIndex(
    mappedTrackInfo: MappingTrackSelector.MappedTrackInfo,
    exoPlayer: ExoPlayer
): Int {
    for (rendererIndex in 0 until mappedTrackInfo.rendererCount) {
        if (exoPlayer.getRendererType(rendererIndex) == C.TRACK_TYPE_AUDIO) {
            return rendererIndex
        }
    }
    return C.INDEX_UNSET
}

//@OptIn(UnstableApi::class)
//fun getAudioTrackInfo(
//    mappedTrackInfo: MappingTrackSelector.MappedTrackInfo,
//    audioTrack: TrackInfo?
//): List<TrackInfo> {
//    val audioTrackInfoList = mutableListOf<TrackInfo>()
//    for (rendererIndex in 0 until mappedTrackInfo.rendererCount) {
//        if (mappedTrackInfo.getRendererType(rendererIndex) == C.TRACK_TYPE_AUDIO) {
//            val trackGroups = mappedTrackInfo.getTrackGroups(rendererIndex)
//            for (groupIndex in 0 until trackGroups.length) {
//                val trackGroup = trackGroups.get(groupIndex)
//                for (trackIndex in 0 until trackGroup.length) {
//                    val format = trackGroup.getFormat(trackIndex)
//                    var language = ""
//                    format.label?.let { language = it }
//                    if (language.isEmpty())
//                        language = getLanguageName(format.language)
//                    val isSel =
//                        audioTrack?.groupIndex == groupIndex && audioTrack.trackIndex == trackIndex
//                    audioTrackInfoList.add(TrackInfo(groupIndex, trackIndex, language, isSel))
//                }
//            }
//        }
//    }
//    return audioTrackInfoList
//}

@OptIn(UnstableApi::class)
fun getAudioTrackInfo(
    mappedTrackInfo: MappingTrackSelector.MappedTrackInfo,
    exoPlayer: ExoPlayer
): List<TrackInfo> {
    val audioTrackInfoList = mutableListOf<TrackInfo>()
    val rendererIndex = getAudioRendererIndex(mappedTrackInfo, exoPlayer)
    if (rendererIndex == C.INDEX_UNSET) return audioTrackInfoList
    for (groupIndex in 0 until mappedTrackInfo.getTrackGroups(rendererIndex).length) {
        val trackGroup = mappedTrackInfo.getTrackGroups(rendererIndex).get(groupIndex)
        for (trackIndex in 0 until trackGroup.length) {
            val format = trackGroup.getFormat(trackIndex)
            var language = ""
            format.label?.let { language = it }
            if (language.isEmpty())
                language = getLanguageName(format.language)
            val id = format.id ?: "a:$language"
            val isSel = run {
                val groups = exoPlayer.currentTracks
                for (group in groups.groups) {
                    if (group.isSelected) {
                        for (i in 0 until group.length) {
                            val formatG = group.getTrackFormat(i)
                            if ((formatG.id ?: "a:${formatG.language ?: ""}") == id)
                                return@run true
                        }
                    }
                }
                return@run false
            }
            audioTrackInfoList.add(TrackInfo(groupIndex, trackIndex, language, isSel))
        }
    }

    return audioTrackInfoList

}

//endregion AUDIO TRACKS

//region VIDEO TRACKS
@OptIn(UnstableApi::class)
fun getVideoRendererIndex(
    mappedTrackInfo: MappingTrackSelector.MappedTrackInfo,
    exoPlayer: ExoPlayer
): Int {
    for (rendererIndex in 0 until mappedTrackInfo.rendererCount) {
        if (exoPlayer.getRendererType(rendererIndex) == C.TRACK_TYPE_VIDEO) {
            return rendererIndex
        }
    }
    return C.INDEX_UNSET
}

@OptIn(UnstableApi::class)
fun getVideoTrackInfo(
    mappedTrackInfo: MappingTrackSelector.MappedTrackInfo,
    videoTrack: TrackInfo?,
): List<TrackInfo> {
    val videoTrackInfoList = mutableListOf<TrackInfo>()
    for (rendererIndex in 0 until mappedTrackInfo.rendererCount) {
        if (mappedTrackInfo.getRendererType(rendererIndex) == C.TRACK_TYPE_VIDEO) {
            val trackGroups = mappedTrackInfo.getTrackGroups(rendererIndex)
            for (groupIndex in 0 until trackGroups.length) {
                val trackGroup = trackGroups.get(groupIndex)
                for (trackIndex in 0 until trackGroup.length) {
                    val format = trackGroup.getFormat(trackIndex)
                    val language = "${format.height}p"
                    val isSel =
                        videoTrack?.groupIndex == groupIndex && videoTrack.trackIndex == trackIndex
                    videoTrackInfoList.add(TrackInfo(groupIndex, trackIndex, language, isSel))
                }
            }
        }
    }
    return videoTrackInfoList
}

//endregion VIDEO TRACKS