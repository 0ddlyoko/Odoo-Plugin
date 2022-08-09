package me.oddlyoko.odoo.modules.models

import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.psi.PyDictLiteralExpression
import com.jetbrains.python.psi.PySequenceExpression
import com.jetbrains.python.psi.PyStringLiteralExpression
import com.jetbrains.python.psi.PyUtil

data class ModuleDescriptor(val directory: PsiDirectory, val manifest: PsiFile, val name: String, val id: String,
                            val version: String, val description: String, val authors: List<String>,
                            val website: String, val depends: List<String>, val data: List<String>,
                            val demo: List<String>, val license: String) {

    override fun toString() = id

    companion object {
        fun fromFile(manifest: PsiFile): ModuleDescriptor? {
            val module: PsiDirectory = manifest.parent ?: return null
            val dict = PsiTreeUtil.findChildOfType(manifest, PyDictLiteralExpression::class.java) ?: return null
            val id = module.name
            var name = ""
            var version = ""
            var description = ""
            var authors = listOf<String>()
            var website = ""
            var depends = listOf<String>()
            var data = listOf<String>()
            var demo = listOf<String>()
            var license = ""

            PyUtil.dictValue(dict).forEach { (key, value) -> run {
                when (key) {
                    "name" -> {
                        if (value is PyStringLiteralExpression)
                            name = value.stringValue
                    }
                    "version" -> {
                        if (value is PyStringLiteralExpression)
                            version = value.stringValue
                    }
                    "description" -> {
                        if (value is PyStringLiteralExpression)
                            description = value.stringValue
                    }
                    "author" -> {
                        when (value) {
                            is PyStringLiteralExpression -> authors = listOf(value.stringValue)
                            is PySequenceExpression -> authors = seqToList(value)
                        }
                    }
                    "website" -> {
                        if (value is PyStringLiteralExpression)
                            website = value.stringValue
                    }
                    "depends" -> {
                        if (value is PySequenceExpression)
                            depends = seqToList(value)
                    }
                    "data" -> {
                        if (value is PySequenceExpression)
                            data = seqToList(value)
                    }
                    "demo" -> {
                        if (value is PySequenceExpression)
                            demo = seqToList(value)
                    }
                    "license" -> {
                        if (value is PyStringLiteralExpression)
                            license = value.stringValue
                    }
                }
            } }

            return ModuleDescriptor(module, manifest, id, version, description, name, authors, website, depends, data, demo, license)
        }

        private fun seqToList(seq: PySequenceExpression): List<String> = PyUtil.strListValue(seq) ?: listOf()
    }
}
