package com.teocoding.exportmycontacts.domain

sealed class PdfPageSize(val width: Int, val height: Int) {

    data object FormatA4 : PdfPageSize(width = 595, height = 842)
}
