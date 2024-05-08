@file:OptIn(ExperimentalFoundationApi::class)

package com.teocoding.exportmycontacts.presentation.screen.contacts_selector

import android.telephony.PhoneNumberUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.teocoding.exportmycontacts.R
import com.teocoding.exportmycontacts.Screen
import com.teocoding.exportmycontacts.domain.model.Email
import com.teocoding.exportmycontacts.domain.model.Person
import com.teocoding.exportmycontacts.domain.model.PersonWithContacts
import com.teocoding.exportmycontacts.domain.model.PhoneNumber
import com.teocoding.exportmycontacts.ui.theme.ExportMyContactsTheme
import java.util.Locale

@Composable
fun ContactsSelectorScreen(
    onEvent: (ContactsSelectorEvent) -> Unit,
    goToScreen: (String) -> Unit,
    screenState: ContactsSelectorState
) {

    LaunchedEffect(key1 = screenState.contactsToExport) {

        screenState.contactsToExport?.let {

            goToScreen(
                Screen.PdfGenerator.createRoute(it)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = 96.dp
            )
        ) {

            item {
                Text(
                    text = stringResource(R.string.select_the_contacts_to_export),
                    style = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    val areAllSelected by remember(
                        key1 = screenState.contacts,
                        key2 = screenState.selectedContacts
                    ) {

                        derivedStateOf {
                            screenState.contacts.isNotEmpty() &&
                                    screenState.selectedContacts.size == screenState.contacts.size
                        }
                    }

                    Checkbox(
                        checked = areAllSelected,
                        onCheckedChange = { isSelected ->

                            onEvent(ContactsSelectorEvent.OnSelectAllChange(isSelected))
                        }
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = stringResource(R.string.select_all),
                        style = MaterialTheme.typography.titleLarge
                    )

                }

                HorizontalDivider()
            }



            items(
                items = screenState.contacts,
                key = { it.person.id }
            ) { person ->

                ContactCard(
                    personWithContacts = person,
                    onSelectionChange = {
                        onEvent(ContactsSelectorEvent.OnPersonSelectChange(it))
                    },
                    isSelected = { person in screenState.selectedContacts }
                )

            }

        }


        ExtendedFloatingActionButton(
            onClick = {
                onEvent(ContactsSelectorEvent.OnExportContacts)
            },
            modifier = Modifier
                .align(
                    Alignment.BottomEnd
                )
                .padding(32.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_arrow_circle_up_24dp),
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(text = stringResource(R.string.export))
        }

        if (screenState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (screenState.isSelectedContactsEmptyError) {

            AlertDialog(
                onDismissRequest = { onEvent(ContactsSelectorEvent.DismissErrorDialog) },
                confirmButton = {
                    TextButton(
                        onClick = { onEvent(ContactsSelectorEvent.DismissErrorDialog) }
                    ) {
                        Text(text = stringResource(R.string.ok))
                    }
                },
                text = {
                    Text(text = stringResource(R.string.select_at_least_one_contact_to_export))
                }
            )

        }
    }

}


@Composable
private fun ContactCard(
    personWithContacts: PersonWithContacts,
    onSelectionChange: (PersonWithContacts) -> Unit,
    isSelected: () -> Boolean,
    modifier: Modifier = Modifier
) {

    val person = personWithContacts.person
    val phoneNumbers = personWithContacts.phoneNumbers
    val emails = personWithContacts.emails

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {


        Checkbox(
            checked = isSelected(),
            onCheckedChange = {
                onSelectionChange(personWithContacts)
            }
        )

        Spacer(modifier = Modifier.width(16.dp))

        Card {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    person.photoUri?.let {

                        AsyncImage(
                            model = person.photoUri,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(50))
                        )


                    } ?: run {

                        Image(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(50))
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = person.name ?: stringResource(id = R.string.no_name),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.basicMarquee()
                    )

                }

                Spacer(modifier = Modifier.height(8.dp))

                HorizontalDivider(thickness = 1.dp)

                Spacer(modifier = Modifier.height(16.dp))

                if (phoneNumbers.isNotEmpty()) {

                    PhoneNumbersSection(
                        phoneNumbers = phoneNumbers,
                        modifier = Modifier
                            .fillMaxWidth()
                    )

                }


                if (emails.isNotEmpty()) {

                    Spacer(modifier = Modifier.height(16.dp))

                    EmailsSection(
                        emails = emails,
                        modifier = Modifier
                            .fillMaxWidth()
                    )

                }
            }
        }
    }

}


@Composable
private fun PhoneNumbersSection(
    phoneNumbers: List<PhoneNumber>,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Outlined.Phone,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = stringResource(R.string.phone_numbers_label),
                style = MaterialTheme.typography.titleSmall
            )

        }

        phoneNumbers.forEachIndexed { index, phone ->


            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {


                Text(
                    text = phone.phoneLabel
                        ?: stringResource(id = R.string.default_label),
                    style = MaterialTheme.typography.labelMedium
                )

                Spacer(modifier = Modifier.width(8.dp))


                Text(
                    text = PhoneNumberUtils.formatNumber(
                        phone.phoneNumber,
                        Locale.getDefault().country
                    )
                        ?: stringResource(id = R.string.no_phone_number),
                    style = MaterialTheme.typography.bodyMedium
                )

            }

            if (index < phoneNumbers.lastIndex) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

}


@Composable
private fun EmailsSection(
    emails: List<Email>,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Outlined.Email,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = stringResource(R.string.emails_label),
                style = MaterialTheme.typography.titleSmall
            )

        }

        emails.forEachIndexed { index, email ->

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {


                Text(
                    text = email.emailLabel
                        ?: stringResource(id = R.string.default_label),
                    style = MaterialTheme.typography.labelMedium
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = email.email ?: stringResource(id = R.string.no_email),
                    style = MaterialTheme.typography.bodyMedium
                )

            }

            if (index < emails.lastIndex) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }


}


@Preview
@PreviewFontScale
@Composable
private fun ContactCardPreview(
    @PreviewParameter(
        PersonWithContactsPreviewParameterProvider::class,
        limit = 1
    ) personWithContacts: PersonWithContacts
) {


    ExportMyContactsTheme {

        ContactCard(
            personWithContacts = personWithContacts,
            onSelectionChange = {},
            isSelected = { true }
        )
    }

}

private class PersonWithContactsPreviewParameterProvider :
    PreviewParameterProvider<PersonWithContacts> {
    override val values = sequenceOf(
        PersonWithContacts(
            person = Person(
                id = "",
                name = "Person 1",
                photoUri = null
            ),
            phoneNumbers = listOf(
                PhoneNumber(
                    id = "",
                    phoneNumber = "+44123456",
                    phoneLabel = "Work",
                    accountType = null,
                    accountName = null
                ),

                PhoneNumber(
                    id = "",
                    phoneNumber = "+4412345678",
                    phoneLabel = "Home",
                    accountType = null,
                    accountName = null
                ),
            ),
            emails = listOf(
                Email(
                    id = "",
                    email = "work@google.com",
                    emailLabel = "Work",
                    accountType = null,
                    accountName = null
                ),
                Email(
                    id = "",
                    email = "home@google.com",
                    emailLabel = "Home",
                    accountType = null,
                    accountName = null
                )
            )
        )
    )

}


