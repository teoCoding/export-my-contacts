@file:OptIn(ExperimentalMaterial3Api::class)

package com.teocoding.exportmycontacts.presentation.screen.pdf_generator

import android.graphics.Picture
import android.telephony.PhoneNumberUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.teocoding.exportmycontacts.R
import com.teocoding.exportmycontacts.Screen
import com.teocoding.exportmycontacts.domain.PdfPageSize
import com.teocoding.exportmycontacts.domain.model.Email
import com.teocoding.exportmycontacts.domain.model.PersonWithContacts
import com.teocoding.exportmycontacts.domain.model.PhoneNumber
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.max


@Composable
fun PdfGeneratorScreen(
    screenState: PdfGeneratorState,
    onEvent: (PdfGeneratorEvent) -> Unit,
    goToScreen: (String) -> Unit
) {

    LaunchedEffect(key1 = screenState.filePath) {
        screenState.filePath?.let {
            goToScreen(Screen.PdfPreview.createRoute(it))
        }
    }

    Box(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
    ) {

        PdfPageMeter(
            people = screenState.people,
            pdfPageSize = screenState.pdfPageSize,
            onContentMeasured = { content ->
                onEvent(PdfGeneratorEvent.OnContentMeasured(content))
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0f)
        ) {

            FirstPagePdf(
                pdfPageSize = screenState.pdfPageSize,
                modifier = Modifier
                    .takeContentPicture { picture ->
                        onEvent(PdfGeneratorEvent.OnFirstPageCreated(picture))
                    }
            )

            if (screenState.currentProcessedIndex in 0..screenState.measuredContent.lastIndex) {

                PdfPage(
                    people = screenState.measuredContent[screenState.currentProcessedIndex],
                    pdfPageSize = screenState.pdfPageSize,
                    modifier = Modifier
                        .takeContentPicture { picture ->
                            onEvent(
                                PdfGeneratorEvent.OnPictureTaken(picture = picture)
                            )
                        }
                )

            }

        }

        PdfCreatorDialog(
            progress = screenState.progress
        )

    }

}

@Composable
private fun PdfCreatorDialog(
    progress: Float,
    modifier: Modifier = Modifier
) {

    BasicAlertDialog(onDismissRequest = { }, modifier = modifier) {

        Column(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(
                        AlertDialogDefaults.TonalElevation
                    ),
                    shape = MaterialTheme.shapes.medium
                )
                .clip(MaterialTheme.shapes.medium)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            Text(
                text = stringResource(R.string.creating_your_pdf),
                style = MaterialTheme.typography.titleMedium
            )

            if (progress < 0.99f) {

                LinearProgressIndicator(
                    progress = {
                        progress
                    },
                    strokeCap = StrokeCap.Round
                )
            } else {
                LinearProgressIndicator(
                    strokeCap = StrokeCap.Round
                )
            }


            val percentFormat = remember {
                NumberFormat.getPercentInstance(Locale.getDefault())
            }

            Text(
                text = percentFormat.format(progress),
                style = MaterialTheme.typography.labelLarge
            )
        }

    }

}

/**
 * Calculates the size of the cards and returns the given list divided by page
 * based on [pdfPageSize]
 *
 * @param [people] the original list of [PersonWithContacts] and map it to [ContactCardPdf]
 * @param [onContentMeasured] callback to get the lists for each pdf page
 * @param [pdfPageSize] the size of the pdf page
 * @param [borderMargin] the margin for the sides of the page in Px
 * @param [cardMargin] the margin between the cards in Px
 *
 */
@Composable
private fun PdfPageMeter(
    people: List<PersonWithContacts>,
    onContentMeasured: (List<List<PersonWithContacts>>) -> Unit,
    pdfPageSize: PdfPageSize,
    borderMargin: Int = 32,
    cardMargin: Int = 16
) {

    Layout(content = {
        people.map { person ->
            ContactCardPdf(personWithContacts = person)
        }
    }) { measurables, constraints ->


        val cardWidth = (pdfPageSize.width - (borderMargin * 2) - cardMargin) / 2

        val placeables = measurables.map {
            it.measure(
                constraints.copy(
                    minWidth = cardWidth,
                    maxWidth = cardWidth
                )
            )
        }

        val rowList = placeables.chunked(2)

        val netPageHeight = pdfPageSize.height - borderMargin * 2
        var accumulatedHeight = 0
        val placeablesInPage = mutableListOf<Placeable>()
        val pagePlaceables = mutableListOf<List<Placeable>>()

        rowList.forEachIndexed { index, placeablesSubList ->

            val maxHeight = placeablesSubList.maxOfOrNull { it.measuredHeight } ?: 0

            accumulatedHeight += (maxHeight + cardMargin)

            if (accumulatedHeight < netPageHeight) {

                placeablesInPage.addAll(placeablesSubList.toList())

            } else {

                pagePlaceables.add(placeablesInPage.toList())
                placeablesInPage.clear()
                placeablesInPage.addAll(placeablesSubList.toList())
                accumulatedHeight = (maxHeight + cardMargin)

            }

            if (index == rowList.lastIndex && placeablesInPage.isNotEmpty()) {
                pagePlaceables.add(placeablesInPage.toList())
            }

        }

        val pages = pagePlaceables.mapIndexed { index, pageItems ->

            if (index == 0) {
                people.subList(0, pageItems.size)
            } else {

                val previousSize = pagePlaceables.subList(0, index).sumOf { it.size }

                people.subList(previousSize, previousSize + pageItems.size)
            }

        }

        onContentMeasured(pages)


        layout(width = 0, height = 0) {

        }
    }

}

@Composable
private fun FirstPagePdf(
    pdfPageSize: PdfPageSize,
    modifier: Modifier = Modifier
) {

    val density = LocalDensity.current

    val pdfHeightDp = remember {
        with(density) {
            pdfPageSize.height.toDp()
        }
    }

    val pdfWidthDp = remember {
        with(density) {
            pdfPageSize.width.toDp()
        }
    }

    Column(
        modifier = modifier
            .size(
                width = pdfWidthDp,
                height = pdfHeightDp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {


        Image(
            painter = painterResource(id = R.drawable.phonebook_512dp),
            contentDescription = null,
            modifier = Modifier.size(100.dp)
        )


        Text(
            text = stringResource(R.string.my_phone_book),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        )
    }

}


@Composable
private fun ContactCardPdf(
    personWithContacts: PersonWithContacts,
    modifier: Modifier = Modifier
) {

    val person = personWithContacts.person
    val phoneNumbers = personWithContacts.phoneNumbers
    val emails = personWithContacts.emails

    val density = LocalDensity.current

    val smallPadding = remember {
        with(density) {
            8.toDp()
        }
    }

    val padding = remember {
        with(density) {
            16.toDp()
        }
    }

    val imageSize = remember {
        with(density) {
            24.toDp()
        }
    }

    val fontSize = remember {
        with(density) {
            16.toSp()
        }
    }

    val cardBorderWidth = remember {
        with(density) {
            1.toDp()
        }
    }

    Card(
        shape = MaterialTheme.shapes.extraSmall,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(
            width = cardBorderWidth,
            color = Color.DarkGray.copy(alpha = 0.50f)
        )
    ) {

        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(padding)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                person.photoUri?.let {

                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .allowHardware(false)
                            .data(person.photoUri)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(imageSize)
                            .clip(RoundedCornerShape(50))
                    )

                } ?: run {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .size(imageSize)
                            .clip(RoundedCornerShape(50))
                    )
                }

                Spacer(modifier = Modifier.width(padding))

                Text(
                    text = person.name ?: stringResource(id = R.string.no_name),
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = fontSize,
                    lineHeight = fontSize
                )

            }

            Spacer(modifier = Modifier.height(smallPadding))

            HorizontalDivider(thickness = cardBorderWidth, color = Color.DarkGray.copy(alpha = 0.50f))

            Spacer(modifier = Modifier.height(padding))

            if (phoneNumbers.isNotEmpty()) {

                PhoneNumbersSectionPdf(
                    phoneNumbers = phoneNumbers,
                    modifier = Modifier
                        .fillMaxWidth()
                )

            }


            if (emails.isNotEmpty()) {

                Spacer(modifier = Modifier.height(padding))

                EmailsSectionPdf(
                    emails = emails,
                    modifier = Modifier
                        .fillMaxWidth()
                )

            }
        }
    }
}

@Composable
private fun PhoneNumbersSectionPdf(
    phoneNumbers: List<PhoneNumber>,
    modifier: Modifier = Modifier
) {

    val density = LocalDensity.current

    val mediumPadding = remember {
        with(density) {
            8.toDp()
        }
    }

    val largePadding = remember {
        with(density) {
            12.toDp()
        }
    }

    val iconSize = remember {
        with(density) {
            16.toDp()
        }
    }

    val titleFontSize = remember {
        with(density) {
            14.toSp()
        }
    }

    val mediumLabelFontSize = remember {
        with(density) {
            12.toSp()
        }
    }

    val mediumBodyFontSize = remember {
        with(density) {
            14.toSp()
        }
    }

    Column(
        modifier = modifier
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = largePadding),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Outlined.Phone,
                contentDescription = null,
                modifier = Modifier.size(iconSize)
            )

            Spacer(modifier = Modifier.width(mediumPadding))

            Text(
                text = stringResource(R.string.phone_numbers_label),
                style = MaterialTheme.typography.titleSmall,
                fontSize = titleFontSize,
                lineHeight = titleFontSize
            )

        }

        phoneNumbers.forEachIndexed { index, phone ->

            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {

                Text(
                    text = phone.phoneLabel
                        ?: stringResource(id = R.string.default_label),
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = mediumLabelFontSize,
                    lineHeight = mediumLabelFontSize
                )

                Spacer(modifier = Modifier.height(mediumPadding))

                Text(
                    text = PhoneNumberUtils.formatNumber(
                        phone.phoneNumber,
                        Locale.getDefault().country
                    )
                        ?: stringResource(id = R.string.no_phone_number),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = mediumBodyFontSize,
                    lineHeight = mediumBodyFontSize
                )

            }

            if (index < phoneNumbers.lastIndex) {
                Spacer(modifier = Modifier.height(mediumPadding))
            }
        }
    }

}


@Composable
private fun EmailsSectionPdf(
    emails: List<Email>,
    modifier: Modifier = Modifier
) {

    val density = LocalDensity.current

    val mediumPadding = remember {
        with(density) {
            8.toDp()
        }
    }

    val largePadding = remember {
        with(density) {
            12.toDp()
        }
    }

    val iconSize = remember {
        with(density) {
            16.toDp()
        }
    }

    val smallTitleFontSize = remember {
        with(density) {
            14.toSp()
        }
    }

    val mediumLabelFontSize = remember {
        with(density) {
            12.toSp()
        }
    }

    val mediumBodyFontSize = remember {
        with(density) {
            14.toSp()
        }
    }

    Column(
        modifier = modifier
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = largePadding),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Outlined.Email,
                contentDescription = null,
                modifier = Modifier.size(iconSize)
            )

            Spacer(modifier = Modifier.width(mediumPadding))

            Text(
                text = stringResource(R.string.emails_label),
                style = MaterialTheme.typography.titleSmall,
                fontSize = smallTitleFontSize,
                lineHeight = smallTitleFontSize
            )

        }

        emails.forEachIndexed { index, email ->

            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {

                Text(
                    text = email.emailLabel
                        ?: stringResource(id = R.string.default_label),
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = mediumLabelFontSize,
                    lineHeight = mediumLabelFontSize
                )

                Spacer(modifier = Modifier.height(mediumPadding))

                Text(
                    text = email.email
                        ?: stringResource(id = R.string.no_email),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = mediumBodyFontSize,
                    lineHeight = mediumBodyFontSize
                )

            }

            if (index < emails.lastIndex) {
                Spacer(modifier = Modifier.height(mediumPadding))
            }
        }
    }

}

@Composable
private fun PdfPage(
    people: List<PersonWithContacts>,
    pdfPageSize: PdfPageSize,
    modifier: Modifier = Modifier,
    borderMargin: Int = 32,
    cardMargin: Int = 16
) {

    Layout(modifier = modifier, content = {
        people.map { person ->
            ContactCardPdf(personWithContacts = person)
        }
    }) { measurables, constraints ->


        val cardWidth = (pdfPageSize.width - (borderMargin * 2) - cardMargin) / 2

        val placeables = measurables.map {
            it.measure(
                constraints.copy(
                    minWidth = cardWidth,
                    maxWidth = cardWidth
                )
            )
        }

        layout(width = pdfPageSize.width, height = pdfPageSize.height) {

            var y = borderMargin

            placeables.forEachIndexed { index, placeable ->

                if (index % 2 == 0) {

                    placeable.place(
                        x = borderMargin,
                        y = y
                    )
                } else {
                    placeable.place(
                        x = cardWidth + borderMargin + cardMargin,
                        y = y
                    )

                    y += max(placeables[index - 1].height, placeable.height) + cardMargin

                }
            }

        }
    }

}

@Composable
private fun Modifier.takeContentPicture(
    onPictureTaken: (Picture) -> Unit
) = drawWithCache {

    val width = this.size.width.toInt()
    val height = this.size.height.toInt()

    val picture = Picture()
    onDrawWithContent {

        val pictureCanvas =
            Canvas(
                picture.beginRecording(
                    width,
                    height
                )
            )
        draw(this, this.layoutDirection, pictureCanvas, this.size) {
            this@onDrawWithContent.drawContent()
        }
        picture.endRecording()

        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawPicture(
                picture
            )
        }

        onPictureTaken(picture)
    }

}
