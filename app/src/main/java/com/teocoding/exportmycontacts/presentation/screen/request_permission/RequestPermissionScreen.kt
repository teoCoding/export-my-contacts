package com.teocoding.exportmycontacts.presentation.screen.request_permission

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.teocoding.exportmycontacts.R
import com.teocoding.exportmycontacts.Screen

@Composable
fun RequestPermissionScreen(
    goToScreen: (String) -> Unit,
) {

    val context = LocalContext.current

    var showRationaleDialog by remember {
        mutableStateOf(false)
    }

    var showNoContactPermissionDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(key1 = Unit) {
        if (
            ContextCompat
                .checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            goToScreen(Screen.ContactsSelector.route)
        }
    }


    val contactPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->

        if (isGranted) {
            goToScreen(Screen.ContactsSelector.route)
        } else {
            showNoContactPermissionDialog = true
        }

    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {


        Button(
            onClick = {
                when {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_CONTACTS
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        goToScreen(Screen.ContactsSelector.route)
                    }

                    ActivityCompat.shouldShowRequestPermissionRationale(
                        context as Activity, Manifest.permission.READ_CONTACTS
                    ) -> {

                        showRationaleDialog = true
                    }

                    else -> {
                        contactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    }

                }


            }

        ) {
            Text(text = stringResource(R.string.import_contacts))
        }


        if (showRationaleDialog) {
            AlertDialog(
                onDismissRequest = { showRationaleDialog = false },
                confirmButton = {

                    TextButton(
                        onClick = {
                            contactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                            showRationaleDialog = false
                        }
                    ) {
                        Text(text = stringResource(id = R.string.ok))
                    }

                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showRationaleDialog = false
                        }
                    ) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                },
                text = {
                    Text(text = stringResource(R.string.rationale_contacts_permission_text))
                }
            )
        }


        if (showNoContactPermissionDialog) {
            AlertDialog(
                onDismissRequest = { showNoContactPermissionDialog = false },
                confirmButton = {

                    TextButton(
                        onClick = {
                            showNoContactPermissionDialog = false
                        }
                    ) {
                        Text(text = stringResource(id = R.string.ok))
                    }

                },
                text = {
                    Text(text = stringResource(R.string.no_contacts_permission_text))
                }
            )
        }

    }

}