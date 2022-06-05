package com.haishinkit.app

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.haishinkit.graphics.PixelTransformFactory
import com.haishinkit.app.R
import com.haishinkit.vulkan.VkPixelTransform

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private var fragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PixelTransformFactory.registerPixelTransform(VkPixelTransform::class)
        }

        setContentView(R.layout.activity_main)

        val navigation = findViewById<BottomNavigationView>(R.id.navigation)
        navigation.setOnNavigationItemSelectedListener(this)

        fragment = CameraTabFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.content, fragment as Fragment).commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_home -> {
                fragment = CameraTabFragment.newInstance()
            }
            R.id.navigation_mediaprojection -> {
                fragment = MediaProjectionTabFragment.newInstance()
            }
            R.id.navigation_playback -> {
                fragment = PlaybackTabFragment.newInstance()
            }
            R.id.navigation_dashboard -> {
                fragment = PreferenceTagFragment.newInstance()
            }
            else -> {
            }
        }
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.content, fragment as Fragment).commit()
        return true
    }
}
