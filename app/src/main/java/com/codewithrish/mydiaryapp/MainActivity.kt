package com.codewithrish.mydiaryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.codewithrish.mydiaryapp.data.repository.MongoDB
import com.codewithrish.mydiaryapp.navigation.Screen
import com.codewithrish.mydiaryapp.navigation.SetupNavGraph
import com.codewithrish.mydiaryapp.ui.theme.DiaryAppTheme
import com.codewithrish.mydiaryapp.util.Constants.APP_ID
import io.realm.kotlin.mongodb.App

class MainActivity : ComponentActivity() {
    private var keepSplashOpened = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition {
            keepSplashOpened
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            DiaryAppTheme {
                val navController = rememberNavController()
                SetupNavGraph(
                    startDestination = getStartDestination(),
                    navController = navController,
                    onDataLoaded = {
                        keepSplashOpened = false
                    }
                )
            }
        }
    }
}

private fun getStartDestination(): String {
    val user = App.create(APP_ID).currentUser
    return if (user != null && user.loggedIn) Screen.Home.route else Screen.Authentication.route
}