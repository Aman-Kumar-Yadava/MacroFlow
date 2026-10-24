package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object MyMacros : Screen("my_macros")
    object MacroEditor : Screen("macro_editor/{macroId}") {
        fun createRoute(macroId: String) = "macro_editor/$macroId"
    }
    object MacroPreview : Screen("macro_preview/{macroId}") {
        fun createRoute(macroId: String) = "macro_preview/$macroId"
    }
    object RecordingWizard : Screen("recording_wizard?macroName={macroName}") {
        fun createRoute(macroName: String = "New Macro") = "recording_wizard?macroName=$macroName"
    }
    object FloatingControls : Screen("floating_controls")
    object ActivityDiagnostics : Screen("activity_diagnostics")
    object Settings : Screen("settings")
}
