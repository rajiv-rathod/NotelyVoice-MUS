package com.module.notelycompose.ai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.ai.domain.model.AiModel
import com.module.notelycompose.ai.presentation.AiNotesViewModel
import com.module.notelycompose.notes.ui.detail.AndroidNoteTopBar
import com.module.notelycompose.notes.ui.detail.IOSNoteTopBar
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.platform.getPlatform
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.ai_settings_api_key_hint
import com.module.notelycompose.resources.ai_settings_api_key_label
import com.module.notelycompose.resources.ai_settings_best_model
import com.module.notelycompose.resources.ai_settings_cheap_model
import com.module.notelycompose.resources.ai_settings_clear_key
import com.module.notelycompose.resources.ai_settings_model_label
import com.module.notelycompose.resources.ai_settings_org_hint
import com.module.notelycompose.resources.ai_settings_org_label
import com.module.notelycompose.resources.ai_settings_save
import com.module.notelycompose.resources.ai_settings_security_note
import com.module.notelycompose.resources.ai_settings_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AiSettingsScreen(
    navigateBack: () -> Unit,
    aiNotesViewModel: AiNotesViewModel = koinViewModel()
) {
    val colors = LocalCustomColors.current
    val savedApiKey by aiNotesViewModel.apiKey.collectAsState()
    val savedOrgId by aiNotesViewModel.orgId.collectAsState()
    val savedModel by aiNotesViewModel.aiModel.collectAsState()

    var apiKeyInput by remember { mutableStateOf("") }
    var orgIdInput by remember { mutableStateOf("") }
    var showApiKey by remember { mutableStateOf(false) }
    var selectedModel by remember { mutableStateOf(AiModel.CHEAP) }

    LaunchedEffect(savedApiKey, savedOrgId, savedModel) {
        if (apiKeyInput.isEmpty() && savedApiKey.isNotEmpty()) {
            apiKeyInput = savedApiKey
        }
        if (orgIdInput.isEmpty() && savedOrgId.isNotEmpty()) {
            orgIdInput = savedOrgId
        }
        selectedModel = savedModel
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bodyBackgroundColor)
    ) {
        if (getPlatform().isAndroid) {
            AndroidNoteTopBar(
                title = stringResource(Res.string.ai_settings_title),
                onNavigateBack = navigateBack
            )
        } else {
            IOSNoteTopBar(
                onNavigateBack = navigateBack
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // API Key section
            AiSettingsSectionHeader("OPENAI API KEY")

            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = { apiKeyInput = it },
                label = { Text(stringResource(Res.string.ai_settings_api_key_label)) },
                placeholder = { Text(stringResource(Res.string.ai_settings_api_key_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    TextButton(onClick = { showApiKey = !showApiKey }) {
                        Text(
                            text = if (showApiKey) "Hide" else "Show",
                            fontSize = 12.sp,
                            color = colors.bodyContentColor.copy(alpha = 0.6f)
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.bodyContentColor,
                    unfocusedTextColor = colors.bodyContentColor,
                    focusedBorderColor = colors.bodyContentColor,
                    unfocusedBorderColor = colors.bodyContentColor.copy(alpha = 0.4f),
                    focusedLabelColor = colors.bodyContentColor,
                    unfocusedLabelColor = colors.bodyContentColor.copy(alpha = 0.6f),
                    cursorColor = colors.bodyContentColor
                )
            )

            // Org ID section
            OutlinedTextField(
                value = orgIdInput,
                onValueChange = { orgIdInput = it },
                label = { Text(stringResource(Res.string.ai_settings_org_label)) },
                placeholder = { Text(stringResource(Res.string.ai_settings_org_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.bodyContentColor,
                    unfocusedTextColor = colors.bodyContentColor,
                    focusedBorderColor = colors.bodyContentColor,
                    unfocusedBorderColor = colors.bodyContentColor.copy(alpha = 0.4f),
                    focusedLabelColor = colors.bodyContentColor,
                    unfocusedLabelColor = colors.bodyContentColor.copy(alpha = 0.6f),
                    cursorColor = colors.bodyContentColor
                )
            )

            Text(
                text = stringResource(Res.string.ai_settings_security_note),
                fontSize = 11.sp,
                color = colors.bodyContentColor.copy(alpha = 0.55f)
            )

            HorizontalDivider(color = colors.bodyContentColor.copy(alpha = 0.12f))

            // Model selection
            AiSettingsSectionHeader(stringResource(Res.string.ai_settings_model_label))

            AiModel.entries.forEach { model ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedModel = model }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedModel == model,
                        onClick = { selectedModel = model },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = colors.bodyContentColor,
                            unselectedColor = colors.bodyContentColor.copy(alpha = 0.4f)
                        )
                    )
                    Column {
                        Text(
                            text = if (model == AiModel.CHEAP)
                                stringResource(Res.string.ai_settings_cheap_model)
                            else
                                stringResource(Res.string.ai_settings_best_model),
                            fontSize = 14.sp,
                            color = colors.bodyContentColor,
                            fontWeight = if (selectedModel == model) FontWeight.SemiBold else FontWeight.Normal
                        )
                        Text(
                            text = model.apiName,
                            fontSize = 11.sp,
                            color = colors.bodyContentColor.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            HorizontalDivider(color = colors.bodyContentColor.copy(alpha = 0.12f))

            // Save button
            Button(
                onClick = {
                    aiNotesViewModel.saveApiKey(apiKeyInput)
                    aiNotesViewModel.saveOrgId(orgIdInput)
                    aiNotesViewModel.setAiModel(selectedModel)
                    navigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.bodyContentColor,
                    contentColor = colors.bodyBackgroundColor
                )
            ) {
                Text(stringResource(Res.string.ai_settings_save), fontSize = 14.sp)
            }

            // Reset / Clear keys
            TextButton(
                onClick = {
                    aiNotesViewModel.clearKeys()
                    apiKeyInput = ""
                    orgIdInput = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(Res.string.ai_settings_clear_key),
                    fontSize = 13.sp,
                    color = colors.bodyContentColor.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AiSettingsSectionHeader(title: String) {
    val colors = LocalCustomColors.current
    Text(
        text = title.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = colors.bodyContentColor.copy(alpha = 0.5f),
        letterSpacing = 0.8.sp
    )
}
