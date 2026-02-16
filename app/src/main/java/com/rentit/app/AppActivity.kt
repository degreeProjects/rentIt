package com.rentit.app

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * AppActivity
 *
 * Main activity for the authenticated app with bottom navigation.
 * Manages navigation between apartments, liked apartments, my apartments, and profile.
 */
class AppActivity : AppCompatActivity() {
    private var navController: NavController? = null // The NavController manages app navigation within the NavHost

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app)

        // initialize navigation controller from nav host fragment
        val navHostFragment: NavHostFragment? =
            supportFragmentManager.findFragmentById(R.id.navHostAppFragment) as? NavHostFragment

        navController = navHostFragment?.navController // Extract the NavController, which performs the actual swapping of fragments

        // setup bottom navigation with nav controller
        val bottomNavigationView: BottomNavigationView =
            findViewById(R.id.mainActivityBottomNavigationView)
        navController?.let { NavigationUI.setupWithNavController(bottomNavigationView, it) }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_app_bar_menu, menu) // Inflate the menu resource defining the icons for the Top App Bar
        
        // force show icons in overflow menu
        if (menu != null) {
            if (menu.javaClass.simpleName == "MenuBuilder") {
                try {
                    val method = menu.javaClass.getDeclaredMethod("setOptionalIconsVisible", Boolean::class.javaPrimitiveType)
                    method.isAccessible = true
                    method.invoke(menu, true)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        
        return true
    }

    // handles top menu item selections and navigation
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navController?.navigateUp()
                true
            }
            else -> navController?.let { NavigationUI.onNavDestinationSelected(item, it) } ?: super.onOptionsItemSelected(item)
        }
    }
}