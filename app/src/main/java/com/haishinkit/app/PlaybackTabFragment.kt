package com.haishinkit.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment

class PlaybackTabFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                PlaybackScreen(
                    command = Preference.shared.rtmpURL,
                    streamName = Preference.shared.streamName,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    companion object {
        fun newInstance(): PlaybackTabFragment {
            return PlaybackTabFragment()
        }

        private val TAG = PlaybackTabFragment::class.java.simpleName
    }
}
