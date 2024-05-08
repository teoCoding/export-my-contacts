package com.teocoding.exportmycontacts

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.teocoding.exportmycontacts.presentation.screen.contacts_selector.ContactsSelectorScreen
import com.teocoding.exportmycontacts.presentation.screen.contacts_selector.ContactsSelectorViewModel
import com.teocoding.exportmycontacts.presentation.screen.pdf_generator.PdfGeneratorScreen
import com.teocoding.exportmycontacts.presentation.screen.pdf_generator.PdfGeneratorViewModel
import com.teocoding.exportmycontacts.presentation.screen.pdf_preview.PdfPreviewScreen
import com.teocoding.exportmycontacts.presentation.screen.pdf_preview.PdfPreviewViewModel
import com.teocoding.exportmycontacts.presentation.screen.request_permission.RequestPermissionScreen
import com.teocoding.exportmycontacts.ui.theme.ExportMyContactsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            ExportMyContactsTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.RequestPermission.route
                    ) {


                        composable(
                            route = Screen.RequestPermission.route,
                            enterTransition = {
                                EnterTransition.None
                            },
                            exitTransition = {
                                this.slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                                    targetOffset = {
                                        (it * 0.33).toInt()
                                    }
                                )
                            }
                        ) {
                            RequestPermissionScreen(
                                goToScreen = {
                                    navController.navigate(Screen.ContactsSelector.route) {
                                        this.popUpTo(Screen.RequestPermission.route) {
                                            this.inclusive = true
                                        }
                                    }
                                }
                            )
                        }


                        composable(
                            route = Screen.ContactsSelector.route,
                            enterTransition = {

                                when (this.initialState.destination.route) {
                                    Screen.PdfPreview.route -> {
                                        this.slideIntoContainer(
                                            towards = AnimatedContentTransitionScope.SlideDirection.End,
                                            initialOffset = {
                                                (it * 0.33).toInt()
                                            }
                                        )
                                    }

                                    else -> {
                                        this.slideIntoContainer(
                                            towards = AnimatedContentTransitionScope.SlideDirection.Start,
                                            initialOffset = {
                                                (it * 0.33).toInt()
                                            }
                                        )
                                    }
                                }

                            },
                            exitTransition = {
                                this.slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                                    targetOffset = {
                                        (it * 0.33).toInt()
                                    }
                                )
                            }
                        ) {

                            val viewModel = hiltViewModel<ContactsSelectorViewModel>()

                            val screenState by viewModel.screenState.collectAsStateWithLifecycle()

                            ContactsSelectorScreen(
                                onEvent = viewModel::onEvent,
                                goToScreen = {
                                    navController.navigate(it)
                                    viewModel.onExportContactsProcessed()
                                },
                                screenState = screenState
                            )
                        }


                        composable(
                            route = Screen.PdfGenerator.route,
                            enterTransition = {
                                EnterTransition.None
                            },
                            exitTransition = {
                                ExitTransition.None
                            },
                            arguments = listOf(
                                navArgument(
                                    name = Screen.PdfGenerator.CONTACTS_IDS
                                ) {
                                    type = NavType.StringType
                                }
                            )
                        ) {

                            val viewModel = hiltViewModel<PdfGeneratorViewModel>()

                            val screenState by viewModel.screenState.collectAsStateWithLifecycle()


                            PdfGeneratorScreen(
                                screenState = screenState,
                                onEvent = viewModel::onEvent,
                                goToScreen = {
                                    navController.navigate(it) {
                                        this.popUpTo(Screen.ContactsSelector.route) {
                                            this.inclusive = false
                                        }
                                    }
                                },
                            )

                        }

                        composable(
                            route = Screen.PdfPreview.route,
                            enterTransition = {
                                this.slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                                    initialOffset = {
                                        (it * 0.33).toInt()
                                    }
                                )
                            },
                            exitTransition = {
                                this.slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.End
                                )
                            },
                            arguments = listOf(
                                navArgument(
                                    name = Screen.PdfPreview.PDF_FILE_PATH
                                ) {
                                    type = NavType.StringType
                                }
                            )
                        ) {

                            val viewModel = hiltViewModel<PdfPreviewViewModel>()

                            val screenState by viewModel.screenState.collectAsStateWithLifecycle()

                            PdfPreviewScreen(
                                onEvent = viewModel::onEvent,
                                screenState = screenState
                            )

                        }

                    }

                }
            }
        }
    }
}


sealed class Screen(val route: String) {

    data object RequestPermission : Screen("request-permission-screen")

    data object ContactsSelector :
        Screen("contact-selector-screen")

    data object PdfGenerator : Screen("pdf-generator-screen/{contactIds}") {

        const val CONTACTS_IDS = "contactIds"

        fun createRoute(contactIds: List<String>): String {

            val contactIdsString = contactIds.joinToString()
            return "pdf-generator-screen/${contactIdsString}"
        }
    }

    data object PdfPreview : Screen("pdf-preview-screen/{filePath}") {

        const val PDF_FILE_PATH = "filePath"

        fun createRoute(pdfFilePath: String): String {

            return "pdf-preview-screen/${Uri.encode(pdfFilePath)}"
        }
    }
}

