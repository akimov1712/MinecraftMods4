package dev.mod.store.minecraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.retainedComponent
import dev.mod.store.minecraft.navigation.DefaultRootComponent
import dev.mod.store.minecraft.navigation.RootContent

/**
 * The single activity. It owns nothing but the retained [DefaultRootComponent] — all screen
 * logic lives in the Decompose component tree (components pull their own dependencies from
 * Koin), and all UI is rendered from [RootContent].
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val root = retainedComponent { componentContext ->
            DefaultRootComponent(componentContext)
        }

        setContent {
            RootContent(component = root)
        }
    }
}
