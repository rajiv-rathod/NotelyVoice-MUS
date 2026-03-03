package com.module.notelycompose.ai.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.module.notelycompose.ai.domain.model.AiNotes
import com.module.notelycompose.ai.domain.model.AiNotesState
import com.module.notelycompose.notes.ui.theme.LocalCustomColors
import com.module.notelycompose.resources.Res
import com.module.notelycompose.resources.ai_notes_action_items
import com.module.notelycompose.resources.ai_notes_error_go_to_settings
import com.module.notelycompose.resources.ai_notes_error_retry
import com.module.notelycompose.resources.ai_notes_generate
import com.module.notelycompose.resources.ai_notes_generating
import com.module.notelycompose.resources.ai_notes_key_terms
import com.module.notelycompose.resources.ai_notes_outline
import com.module.notelycompose.resources.ai_notes_section_title
import com.module.notelycompose.resources.ai_notes_study_questions
import com.module.notelycompose.resources.ai_notes_tldr
import com.module.notelycompose.resources.ai_notes_transcribing
import org.jetbrains.compose.resources.stringResource

@Composable
fun AiNotesSection(
    state: AiNotesState,
    onGenerate: () -> Unit,
    onRetry: () -> Unit,
    onGoToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalCustomColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(Res.string.ai_notes_section_title),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = colors.bodyContentColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            AiStatusChip(state = state)
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (state) {
            is AiNotesState.Idle -> {
                Button(
                    onClick = onGenerate,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.bodyContentColor,
                        contentColor = colors.bodyBackgroundColor
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(Res.string.ai_notes_generate),
                        fontSize = 14.sp
                    )
                }
            }

            is AiNotesState.Transcribing -> {
                AiProgressRow(stringResource(Res.string.ai_notes_transcribing))
            }

            is AiNotesState.GeneratingNotes -> {
                AiProgressRow(stringResource(Res.string.ai_notes_generating))
            }

            is AiNotesState.Ready -> {
                AiNotesContent(notes = state.notes)
            }

            is AiNotesState.Error -> {
                AiErrorContent(
                    message = state.message,
                    isApiKeyMissing = state.isApiKeyMissing,
                    onRetry = onRetry,
                    onGoToSettings = onGoToSettings
                )
            }
        }
    }
}

@Composable
private fun AiStatusChip(state: AiNotesState) {
    val colors = LocalCustomColors.current
    val (label, bgColor) = when (state) {
        is AiNotesState.Idle -> "" to colors.bodyBackgroundColor
        is AiNotesState.Transcribing -> "Transcribing…" to colors.statusBarBackgroundColor
        is AiNotesState.GeneratingNotes -> "Generating…" to colors.statusBarBackgroundColor
        is AiNotesState.Ready -> "Ready" to colors.bodyBackgroundColor
        is AiNotesState.Error -> "Error" to colors.bodyBackgroundColor
    }
    if (label.isNotEmpty()) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .border(1.dp, colors.bodyContentColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(text = label, fontSize = 11.sp, color = colors.bodyContentColor)
        }
    }
}

@Composable
private fun AiProgressRow(label: String) {
    val colors = LocalCustomColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp,
            color = colors.bodyContentColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, fontSize = 13.sp, color = colors.bodyContentColor)
    }
}

@Composable
private fun AiErrorContent(
    message: String,
    isApiKeyMissing: Boolean,
    onRetry: () -> Unit,
    onGoToSettings: () -> Unit
) {
    val colors = LocalCustomColors.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = message,
            fontSize = 13.sp,
            color = colors.bodyContentColor.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (isApiKeyMissing) {
                Button(
                    onClick = onGoToSettings,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.bodyContentColor,
                        contentColor = colors.bodyBackgroundColor
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(Res.string.ai_notes_error_go_to_settings), fontSize = 13.sp)
                }
            } else {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.bodyContentColor,
                        contentColor = colors.bodyBackgroundColor
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(Res.string.ai_notes_error_retry), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun AiNotesContent(notes: AiNotes) {
    val colors = LocalCustomColors.current
    Column(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (notes.title.isNotBlank()) {
            Text(
                text = notes.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = colors.bodyContentColor
            )
        }

        // TL;DR
        if (notes.tldr.isNotEmpty()) {
            AiSection(title = stringResource(Res.string.ai_notes_tldr)) {
                notes.tldr.forEach { bullet ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("• ", fontSize = 14.sp, color = colors.bodyContentColor)
                        Text(bullet, fontSize = 14.sp, color = colors.bodyContentColor)
                    }
                }
            }
        }

        // Outline
        if (notes.outline.isNotEmpty()) {
            AiSection(title = stringResource(Res.string.ai_notes_outline)) {
                notes.outline.forEach { section ->
                    Text(
                        text = section.heading,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = colors.bodyContentColor
                    )
                    section.bullets.forEach { b ->
                        Row(modifier = Modifier.padding(start = 12.dp)) {
                            Text("• ", fontSize = 13.sp, color = colors.bodyContentColor)
                            Text(b, fontSize = 13.sp, color = colors.bodyContentColor)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        // Key Terms
        if (notes.keyTerms.isNotEmpty()) {
            AiSection(title = stringResource(Res.string.ai_notes_key_terms)) {
                notes.keyTerms.forEach { kt ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${kt.term}: ",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = colors.bodyContentColor
                        )
                        Text(kt.definition, fontSize = 13.sp, color = colors.bodyContentColor)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }

        // Action Items
        if (notes.actionItems.isNotEmpty()) {
            AiSection(title = stringResource(Res.string.ai_notes_action_items)) {
                notes.actionItems.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, colors.bodyContentColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.confidence >= 0.8) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp),
                                    tint = colors.bodyContentColor
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(item.task, fontSize = 13.sp, color = colors.bodyContentColor)
                            if (!item.owner.isNullOrBlank() || !item.due.isNullOrBlank()) {
                                Text(
                                    text = listOfNotNull(
                                        item.owner?.let { "Owner: $it" },
                                        item.due?.let { "Due: $it" }
                                    ).joinToString(" · "),
                                    fontSize = 11.sp,
                                    color = colors.bodyContentColor.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        // Study Questions
        if (notes.studyQuestions.isNotEmpty()) {
            AiSection(title = stringResource(Res.string.ai_notes_study_questions)) {
                notes.studyQuestions.forEach { sq ->
                    CollapsibleStudyQuestion(q = sq.q, a = sq.a)
                }
            }
        }
    }
}

@Composable
private fun AiSection(
    title: String,
    content: @Composable () -> Unit
) {
    val colors = LocalCustomColors.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = colors.bodyContentColor.copy(alpha = 0.5f),
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        content()
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = colors.bodyContentColor.copy(alpha = 0.12f))
    }
}

@Composable
private fun CollapsibleStudyQuestion(q: String, a: String) {
    val colors = LocalCustomColors.current
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = q,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.bodyContentColor,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.bodyContentColor,
                modifier = Modifier.size(18.dp)
            )
        }
        AnimatedVisibility(visible = expanded) {
            Text(
                text = a,
                fontSize = 13.sp,
                color = colors.bodyContentColor.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp, start = 8.dp)
            )
        }
    }
}
