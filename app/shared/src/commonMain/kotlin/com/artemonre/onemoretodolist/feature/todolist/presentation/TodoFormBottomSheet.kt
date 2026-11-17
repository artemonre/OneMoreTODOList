package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.SpeechRecognitionState
import com.artemonre.onemoretodolist.core.designsystem.components.AppBottomSheet
import com.artemonre.onemoretodolist.core.designsystem.components.AppCheckToggle
import com.artemonre.onemoretodolist.core.designsystem.components.AppCounter
import com.artemonre.onemoretodolist.core.designsystem.components.keepsKeyboardOnTap
import com.artemonre.onemoretodolist.core.designsystem.components.AppAlertDialog
import com.artemonre.onemoretodolist.core.designsystem.components.AppButton
import com.artemonre.onemoretodolist.core.designsystem.components.AppTextButton
import com.artemonre.onemoretodolist.core.designsystem.theme.AppSpacing
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.presentation.resourceLabels
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.feature.todolist.domain.DueTimeMode
import com.artemonre.onemoretodolist.feature.todolist.domain.Recurrence
import com.artemonre.onemoretodolist.feature.todolist.domain.RecurrenceType
import com.artemonre.onemoretodolist.feature.todolist.domain.RecurrenceUnit
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoTag
import com.artemonre.onemoretodolist.rememberDueTimeNotificationPermissionState
import com.artemonre.onemoretodolist.rememberExactAlarmPermissionState
import com.artemonre.onemoretodolist.rememberSpeechToText
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import onemoretodolist.app.shared.generated.resources.Res
import onemoretodolist.app.shared.generated.resources.form_allow_notifications
import onemoretodolist.app.shared.generated.resources.form_approximate_description
import onemoretodolist.app.shared.generated.resources.form_cancel
import onemoretodolist.app.shared.generated.resources.form_clear_text
import onemoretodolist.app.shared.generated.resources.form_create
import onemoretodolist.app.shared.generated.resources.form_date
import onemoretodolist.app.shared.generated.resources.form_decrease
import onemoretodolist.app.shared.generated.resources.form_default_text
import onemoretodolist.app.shared.generated.resources.form_discard
import onemoretodolist.app.shared.generated.resources.form_due_time
import onemoretodolist.app.shared.generated.resources.form_every
import onemoretodolist.app.shared.generated.resources.form_exact_description
import onemoretodolist.app.shared.generated.resources.form_exact_needs_permission
import onemoretodolist.app.shared.generated.resources.form_exact_permission_title
import onemoretodolist.app.shared.generated.resources.form_exact_time
import onemoretodolist.app.shared.generated.resources.form_grant_permission
import onemoretodolist.app.shared.generated.resources.form_increase
import onemoretodolist.app.shared.generated.resources.form_more_settings
import onemoretodolist.app.shared.generated.resources.form_notifications_off
import onemoretodolist.app.shared.generated.resources.form_ok
import onemoretodolist.app.shared.generated.resources.form_only_after_completion
import onemoretodolist.app.shared.generated.resources.form_pick_date
import onemoretodolist.app.shared.generated.resources.form_pick_time
import onemoretodolist.app.shared.generated.resources.form_put_to_top
import onemoretodolist.app.shared.generated.resources.form_quick_add
import onemoretodolist.app.shared.generated.resources.form_reminder_and_repeat
import onemoretodolist.app.shared.generated.resources.form_repeat
import onemoretodolist.app.shared.generated.resources.form_save
import onemoretodolist.app.shared.generated.resources.form_select_date
import onemoretodolist.app.shared.generated.resources.form_select_time
import onemoretodolist.app.shared.generated.resources.form_speak_text
import onemoretodolist.app.shared.generated.resources.form_stop_listening
import onemoretodolist.app.shared.generated.resources.form_text_label
import onemoretodolist.app.shared.generated.resources.form_text_placeholder
import onemoretodolist.app.shared.generated.resources.form_time
import onemoretodolist.app.shared.generated.resources.form_title_add
import onemoretodolist.app.shared.generated.resources.form_title_edit
import onemoretodolist.app.shared.generated.resources.mascot_put_paws_on_fence
import onemoretodolist.app.shared.generated.resources.mascot_put_paws_on_fence_monochrome
import onemoretodolist.app.shared.generated.resources.recurrence_after_completion
import onemoretodolist.app.shared.generated.resources.recurrence_after_completion_with_reminder
import onemoretodolist.app.shared.generated.resources.recurrence_day
import onemoretodolist.app.shared.generated.resources.recurrence_every
import onemoretodolist.app.shared.generated.resources.recurrence_every_with_reminder
import onemoretodolist.app.shared.generated.resources.recurrence_month
import onemoretodolist.app.shared.generated.resources.recurrence_unit_day
import onemoretodolist.app.shared.generated.resources.recurrence_unit_month
import onemoretodolist.app.shared.generated.resources.recurrence_unit_week
import onemoretodolist.app.shared.generated.resources.recurrence_unit_year
import onemoretodolist.app.shared.generated.resources.recurrence_week
import onemoretodolist.app.shared.generated.resources.recurrence_year
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import com.artemonre.onemoretodolist.core.designsystem.components.AppDatePickerDialog
import com.artemonre.onemoretodolist.core.designsystem.components.AppDivider
import com.artemonre.onemoretodolist.core.designsystem.components.AppIconButton
import com.artemonre.onemoretodolist.core.designsystem.components.AppOutlinedCard
import com.artemonre.onemoretodolist.core.designsystem.components.AppSectionTitle
import com.artemonre.onemoretodolist.core.designsystem.components.AppSegmentedButtonRow
import com.artemonre.onemoretodolist.core.designsystem.components.AppTextField
import com.artemonre.onemoretodolist.core.designsystem.components.AppTimePickerDialog
import com.artemonre.onemoretodolist.core.designsystem.components.appSupportingTextStyle

private val MASCOT_HEIGHT = 80.dp
// Where the sheet's top edge goes, as a fraction of the drawing's height - a bit above the bottom of
// its body, so the body's bottom and the paws rest on the sheet.
private const val MASCOT_FENCE_FRACTION = 0.835f
private val MASCOT_OVERHANG = peekingMascotOverhang(MASCOT_HEIGHT, MASCOT_FENCE_FRACTION)

// editingItem == null means "add" mode; non-null pre-fills the form and confirms as an edit.
// onDismiss is every close that isn't a choice - swipe down, Back, a tap outside - while onDiscard
// is the Discard button: an add host keeps its draft (initialDraft/onDraftChange) on the first
// and drops it on the second.
@Composable
fun TodoFormBottomSheet(
    editingItem: TodoItemUi?,
    onConfirm: (TodoDraft) -> Unit,
    onDismiss: () -> Unit,
    // The host carries the draft (kept up to date through onDraftChange) along to the full-screen
    // dialog, so switching to "More settings" doesn't lose anything.
    onMoreSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    onDiscard: () -> Unit = onDismiss,
    initialDraft: TodoDraft? = null,
    onDraftChange: (TodoDraft) -> Unit = {}
) {
    var isSheetExpanded by remember { mutableStateOf(false) }

    AppBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        onExpanded = { isSheetExpanded = true },
        topDecoration = {
            PeekingMascot(
                mascotDrawable(Res.drawable.mascot_put_paws_on_fence, Res.drawable.mascot_put_paws_on_fence_monochrome),
                MASCOT_HEIGHT
            )
        },
        topDecorationOverhang = MASCOT_OVERHANG
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.l),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.form_quick_add),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() }
            )
            AppTextButton(onClick = onMoreSettingsClick) {
                Text(stringResource(Res.string.form_more_settings))
            }
        }
        TodoFormBody(
            editingItem = editingItem,
            onConfirm = onConfirm,
            onDismiss = onDiscard,
            initialDraft = initialDraft,
            onDraftChange = onDraftChange,
            // A short window (JVM/desktop, or a small/rotated phone) can leave less height than
            // the form needs - without this the Cancel/OK row can end up positioned below the
            // visible sheet instead of being reachable by scrolling.
            modifier = Modifier.verticalScroll(rememberScrollState()),
            // The sheet's own drag handle already separates it from the content above, so the
            // title is redundant here - unlike TodoFormFullScreenDialog, which has nothing else
            // marking it as an add/edit form.
            showTitle = false,
            fieldMaxLines = Int.MAX_VALUE,
            actionsTopSpacing = AppSpacing.s,
            awaitAutoFocusReady = { snapshotFlow { isSheetExpanded }.first { it } }
        )
    }
}

// The form's titled sections, in the order they're shown. Holo prints each one's number, so only
// the sections this host actually shows are counted.
internal enum class FormSection { Tags, Checklist, ReminderAndRepeat }

internal fun formSectionNumbers(
    showTags: Boolean,
    showChecklist: Boolean,
    showReminderAndRepeat: Boolean
): Map<FormSection, Int> =
    listOfNotNull(
        FormSection.Tags.takeIf { showTags },
        FormSection.Checklist.takeIf { showChecklist },
        FormSection.ReminderAndRepeat.takeIf { showReminderAndRepeat }
    ).withIndex().associate { (index, section) -> section to index + 1 }

// Shared by TodoFormBottomSheet (quick-add flow), TodoFormFullScreenDialog (detailed add and edit
// flows), and gateway/todoList's QuickAddTodoActivity (widget quick-add) - same fields and
// behavior, just hosted in a different container. Public since the widget's quick-add screen lives
// in a separate Gradle module.
// fieldMinLines starts at 1 line everywhere and grows with the text - TodoFormBottomSheet and
// TodoFormFullScreenDialog both raise fieldMaxLines to unlimited so it can keep growing; the
// default fieldMaxLines here (2) only applies to QuickAddTodoActivity, which doesn't override it.
// showTitle/actionsTopSpacing default to what the full-screen dialog and quick-add screen use -
// TodoFormBottomSheet tightens both since its drag handle already separates it from whatever is
// above. showDueTime/showRecurrence are off by default too - both are detailed-creation concerns,
// only TodoFormFullScreenDialog turns them on.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoFormBody(
    editingItem: TodoItemUi?,
    onConfirm: (TodoDraft) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true,
    fieldMinLines: Int = 1,
    fieldMaxLines: Int = 2,
    actionsTopSpacing: Dp = 20.dp,
    // Off by default for the same reason as showRecurrence below - detailed scheduling only
    // belongs in TodoFormFullScreenDialog, which is the only caller that turns this on.
    showDueTime: Boolean = false,
    showRecurrence: Boolean = false,
    // Same "detailed creation only" reasoning - only TodoFormFullScreenDialog turns these on.
    // knownTags feeds the tag picker's suggestions and is ignored while showTags is off.
    showTags: Boolean = false,
    showChecklist: Boolean = false,
    knownTags: List<TodoTag> = emptyList(),
    // Off by default: the quick-add bottom sheet and widget already rely on Android's own IME
    // "Done" handling and keep their existing no-op-on-blank behavior. TodoFormFullScreenDialog
    // turns this on since it's the one flow where a stray blank-text save was reachable.
    requireText: Boolean = false,
    // Only used when editingItem == null - seeds every field without pretending it's an edit: the
    // unsaved add draft kept from an accidental close, or a scanned photo continued as one todo.
    initialDraft: TodoDraft? = null,
    // The form's current state on every change, from the first composition on - what an add host
    // keeps as its unsaved draft. Raw, unlike onConfirm's: untrimmed text, blank checklist lines.
    onDraftChange: (TodoDraft) -> Unit = {},
    awaitAutoFocusReady: suspend () -> Unit = {}
) {
    val sectionNumbers = remember(showTags, showChecklist, showDueTime, showRecurrence) {
        formSectionNumbers(showTags, showChecklist, showDueTime || showRecurrence)
    }
    val seed = editingItem?.toTodoDraft() ?: initialDraft
    // Pre-filled text - editing an existing todo, or a kept draft - starts with the cursor at the
    // end instead of the start, which is what you'd want to keep typing from.
    val textState = seed?.text.orEmpty().let { prefilled ->
        rememberTextFieldState(initialText = prefilled, initialSelection = TextRange(prefilled.length))
    }
    var isPrioritized by remember { mutableStateOf(seed?.isPrioritized ?: false) }
    var tags by remember { mutableStateOf(seed?.tags.orEmpty()) }
    var checklist by remember { mutableStateOf(seed?.checklist.orEmpty()) }
    var repeatEnabled by remember { mutableStateOf(seed?.recurrence != null) }
    var recurrenceType by remember { mutableStateOf(seed?.recurrence?.type ?: RecurrenceType.Every) }
    // A counter (not free text) so this is always a valid positive interval - no parsing/validation needed.
    var recurrenceInterval by remember { mutableStateOf(seed?.recurrence?.interval ?: 1) }
    var recurrenceUnit by remember { mutableStateOf(seed?.recurrence?.unit ?: RecurrenceUnit.Day) }
    // Built even while Repeat is off, so the controls' description still has something to show
    // while they animate out.
    val recurrenceDraft = Recurrence(type = recurrenceType, interval = recurrenceInterval, unit = recurrenceUnit)
    val recurrence = recurrenceDraft.takeIf { repeatEnabled }
    // null where exact alarms aren't a distinct grantable permission (pre-Android 12, or any
    // non-Android target) - the "Grant permission" button only shows where it's actually needed.
    val exactAlarmPermission = rememberExactAlarmPermissionState()
    // null on any non-Android target - request() is called once, the first time "Reminder" is
    // turned on (see below); declining doesn't block saving, unlike exactAlarmPermission.
    val notificationPermission = rememberDueTimeNotificationPermissionState()
    // Values survive unchecking "Reminder" the same way recurrenceInterval/Unit do. Prefilled from
    // editingItem, same as the recurrence fields above.
    var dueTimeEnabled by remember { mutableStateOf(seed?.dueDate != null) }
    // Off = approximate (the silent default/fallback, no extra permission needed) - on is an
    // explicit opt-in for exact-time delivery, not a mode the user is forced to pick upfront.
    // Defaults purely off the current permission state, adding or editing alike - no reason to
    // make someone who's already cleared that hurdle opt in again, regardless of whatever mode a
    // given todo happened to be saved with before.
    var exactTimeEnabled by remember {
        mutableStateOf(exactAlarmPermission?.isGranted == true)
    }
    // Wall-clock local values, not a resolved instant - "10am" should keep meaning 10am local even
    // if the timezone changes before it fires, see TodoItem.dueInstant.
    var showExactPermissionDialog by remember { mutableStateOf(false) }
    var dueDate by remember { mutableStateOf(seed?.dueDate) }
    var dueTime by remember { mutableStateOf(seed?.dueTime) }
    var showDueDatePicker by remember { mutableStateOf(false) }
    val shortDateFormat = rememberShortDateFormat()
    var showDueTimePicker by remember { mutableStateOf(false) }
    val dueTimeMode = if (dueTimeEnabled && dueDate != null && dueTime != null) {
        if (exactTimeEnabled) DueTimeMode.Exact else DueTimeMode.Approximate
    } else {
        null
    }
    // Reminder checked but not both fields actually picked would otherwise silently discard the
    // user's intent on save - block it the same way a missing exact-alarm permission is blocked.
    val dueTimeIncomplete = dueTimeEnabled && (dueDate == null || dueTime == null)
    // Re-checked on every recomposition, so a reminder that slips into the past while the form
    // sits open blocks saving as soon as anything else on the form changes.
    val dueDateTime = dueDate?.let { date -> dueTime?.let { time -> LocalDateTime(date, time) } }
    val dueTimeInPast = dueTimeEnabled && dueDateTime != null && dueDateTime < localNow()
    val dueTimeExactPermissionMissing = dueTimeEnabled && exactTimeEnabled && exactAlarmPermission?.isGranted == false
    val canSubmit = (textState.text.isNotBlank() || !requireText) && !dueTimeIncomplete && !dueTimeInPast && !dueTimeExactPermissionMissing
    val openedAt = remember {
        dueTimeFormat.format(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time)
    }
    val defaultText = stringResource(Res.string.form_default_text, openedAt)
    // Blank checklist lines (added but never typed into) are dropped rather than saved as empty
    // items. Blank text falls back to defaultText - only reachable where requireText is off.
    val submit = {
        onConfirm(
            TodoDraft(
                text = textState.text.trim().toString().ifEmpty { defaultText },
                isPrioritized = isPrioritized,
                recurrence = recurrence,
                dueDate = dueDate,
                dueTime = dueTime,
                dueTimeMode = dueTimeMode,
                checklist = checklist
                    .map { it.copy(text = it.text.trim()) }
                    .filter { it.text.isNotEmpty() },
                tags = tags
            )
        )
    }
    // Reads the state holders themselves (not the values computed above), so snapshotFlow sees
    // every change. A turned-off reminder or repeat leaves out the values it keeps for re-enabling,
    // same as submit.
    val currentOnDraftChange by rememberUpdatedState(onDraftChange)
    LaunchedEffect(Unit) {
        snapshotFlow {
            TodoDraft(
                text = textState.text.toString(),
                isPrioritized = isPrioritized,
                recurrence = Recurrence(type = recurrenceType, interval = recurrenceInterval, unit = recurrenceUnit)
                    .takeIf { repeatEnabled },
                dueDate = dueDate.takeIf { dueTimeEnabled },
                dueTime = dueTime.takeIf { dueTimeEnabled },
                dueTimeMode = if (dueTimeEnabled && dueDate != null && dueTime != null) {
                    if (exactTimeEnabled) DueTimeMode.Exact else DueTimeMode.Approximate
                } else {
                    null
                },
                checklist = checklist,
                tags = tags
            )
        }.collect { currentOnDraftChange(it) }
    }
    val textFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val speechController = rememberSpeechToText(
        onResult = { textState.setTextAndPlaceCursorAtEnd(it) }
    )

    LaunchedEffect(Unit) {
        // Wait for the host (bottom sheet) to finish its entrance animation before grabbing focus -
        // requesting it eagerly races the keyboard's show animation against the sheet's slide-up and
        // makes both visibly stutter.
        awaitAutoFocusReady()
        textFocusRequester.requestFocus()
        keyboardController?.show()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.l)
            .padding(bottom = AppSpacing.l)
    ) {
        if (showTitle) {
            Text(
                text = stringResource(if (editingItem != null) Res.string.form_title_edit else Res.string.form_title_add),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(top = AppSpacing.s)
                    .semantics { heading() }
            )
            Spacer(Modifier.height(AppSpacing.l))
        }
        AppTextField(
            state = textState,
            label = { Text(stringResource(Res.string.form_text_label)) },
            placeholder = { Text(stringResource(Res.string.form_text_placeholder)) },
            lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = fieldMinLines, maxHeightInLines = fieldMaxLines),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            onKeyboardAction = { if (canSubmit) submit() },
            compact = true,
            trailingIcon = if (textState.text.isNotEmpty()) {
                {
                    AppIconButton(onClick = { textState.clearText() }) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = stringResource(Res.string.form_clear_text))
                    }
                }
            } else {
                speechController?.let { controller ->
                    {
                        val isListening = controller.state.value is SpeechRecognitionState.Listening
                        AppIconButton(
                            onClick = { if (isListening) controller.stop() else controller.start() }
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                                contentDescription = stringResource(if (isListening) Res.string.form_stop_listening else Res.string.form_speak_text)
                            )
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(textFocusRequester)
                .keepsKeyboardOnTap()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = isPrioritized,
                    onValueChange = { isPrioritized = it },
                    role = Role.Checkbox
                )
                .padding(vertical = AppSpacing.s),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppCheckToggle(
                checked = isPrioritized,
                onCheckedChange = null
            )
            Spacer(Modifier.width(AppSpacing.m))
            Text(stringResource(Res.string.form_put_to_top))
        }
        if (showTags) {
            Spacer(Modifier.height(AppSpacing.s))
            TodoFormTagsSection(
                tags = tags,
                knownTags = knownTags,
                onTagsChange = { tags = it },
                number = sectionNumbers[FormSection.Tags]
            )
        }
        if (showChecklist) {
            Spacer(Modifier.height(AppSpacing.m))
            TodoFormChecklistSection(
                items = checklist,
                onItemsChange = { checklist = it },
                number = sectionNumbers[FormSection.Checklist]
            )
        }
        if (showDueTime || showRecurrence) {
            Spacer(Modifier.height(AppSpacing.m))
            AppSectionTitle(
                // Without due times on this platform, the section is only Repeat.
                title = stringResource(if (showDueTime) Res.string.form_reminder_and_repeat else Res.string.form_repeat),
                number = sectionNumbers[FormSection.ReminderAndRepeat],
                compact = true
            )
        }
        if (showDueTime) {
            Spacer(Modifier.height(AppSpacing.xs))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = dueTimeEnabled,
                        onValueChange = { newValue ->
                            dueTimeEnabled = newValue
                            // Both delivery modes ultimately post a system notification - ask once,
                            // the first time this is turned on. Declining doesn't block saving.
                            if (newValue) notificationPermission?.request()
                        },
                        role = Role.Checkbox
                    )
                    .padding(vertical = AppSpacing.s),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppCheckToggle(
                    checked = dueTimeEnabled,
                    onCheckedChange = null
                )
                Spacer(Modifier.width(AppSpacing.m))
                Text(stringResource(Res.string.form_due_time))
            }
        }
        if (showDueTime) {
            AnimatedVisibility(visible = dueTimeEnabled) {
                Column {
                    Spacer(Modifier.height(AppSpacing.s))
                    // Notifications being off blocks delivery entirely regardless of exact/approximate
                    // mode, so it takes priority over that mode's own description, never both
                    // stacked. With Repeat on, the mode descriptions give way to the combined one;
                    // the two problems the buttons fix still show above it.
                    val reminderProblem = when {
                        notificationPermission?.isGranted == false -> Res.string.form_notifications_off
                        exactTimeEnabled && exactAlarmPermission?.isGranted == false -> Res.string.form_exact_needs_permission
                        else -> null
                    }
                    val repeatsHere = showRecurrence && repeatEnabled
                    val date = dueDate
                    val time = dueTime
                    val reminderDescription = when {
                        repeatsHere && date != null && time != null ->
                            recurrenceDraft.describeWithReminder(shortDateFormat.format(date), dueTimeFormat.format(time))
                        repeatsHere -> recurrenceDraft.describe()
                        reminderProblem != null -> null
                        !exactTimeEnabled -> stringResource(Res.string.form_approximate_description)
                        else -> stringResource(Res.string.form_exact_description)
                    }
                    // Mirrors the Recurrence section's bordered card below - same convention for a
                    // checkbox-toggled block of detailed settings.
                    AppOutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        // The card's notes, all at the bottom: a delivery problem with the button
                        // that fixes it, then what the reminder (and repeat) will do.
                        footer = {
                            // Cross-fades between problems, and shrinks to nothing when there's none.
                            AnimatedContent(
                                targetState = reminderProblem,
                                transitionSpec = { fadeIn() togetherWith fadeOut() }
                            ) { problem ->
                                problem?.let { FormDescription(stringResource(it)) }
                            }
                            AnimatedVisibility(visible = notificationPermission?.isGranted == false) {
                                AppTextButton(onClick = { notificationPermission?.request(allowSettingsRedirect = true) }) {
                                    Text(stringResource(Res.string.form_allow_notifications))
                                }
                            }
                            exactAlarmPermission?.let { permission ->
                                AnimatedVisibility(
                                    visible = notificationPermission?.isGranted != false && exactTimeEnabled && !permission.isGranted
                                ) {
                                    AppTextButton(onClick = permission::request) {
                                        Text(stringResource(Res.string.form_grant_permission))
                                    }
                                }
                            }
                            reminderDescription?.let { description ->
                                if (reminderProblem != null) Spacer(Modifier.height(AppSpacing.s))
                                FormDescription(description)
                            }
                        }
                    ) {
                        Column(
                            modifier = Modifier.padding(AppSpacing.m)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.s)
                            ) {
                                ReadOnlyPickerField(
                                    value = dueDate?.let { shortDateFormat.format(it) }.orEmpty(),
                                    label = stringResource(Res.string.form_date),
                                    placeholder = stringResource(Res.string.form_select_date),
                                    icon = Icons.Filled.CalendarMonth,
                                    iconContentDescription = stringResource(Res.string.form_pick_date),
                                    onClick = { showDueDatePicker = true },
                                    modifier = Modifier.weight(3f)
                                )
                                ReadOnlyPickerField(
                                    value = dueTime?.let { dueTimeFormat.format(it) }.orEmpty(),
                                    label = stringResource(Res.string.form_time),
                                    placeholder = stringResource(Res.string.form_select_time),
                                    icon = Icons.Filled.Schedule,
                                    iconContentDescription = stringResource(Res.string.form_pick_time),
                                    onClick = { showDueTimePicker = true },
                                    modifier = Modifier.weight(2f)
                                )
                            }
                            // An opt-in, not a mode picker - approximate delivery is the silent default/
                            // fallback (no extra permission needed), so most people never need to touch this.
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .toggleable(
                                        value = exactTimeEnabled,
                                        onValueChange = { checked ->
                                            exactTimeEnabled = checked
                                            if (checked && exactAlarmPermission?.isGranted == false) {
                                                showExactPermissionDialog = true
                                            }
                                        },
                                        role = Role.Checkbox
                                    )
                                    .padding(vertical = AppSpacing.s),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppCheckToggle(
                                    checked = exactTimeEnabled,
                                    onCheckedChange = null
                                )
                                Spacer(Modifier.width(AppSpacing.m))
                                Text(stringResource(Res.string.form_exact_time))
                            }
                            // Repeat moves into the Reminder card while it's open - the two drive each
                            // other (see reachedDueOccurrence/ToggleTodoDone), so they read as one block
                            // with one combined description, instead of two unrelated sections.
                            if (showRecurrence) {
                                AppDivider()
                                RepeatToggleRow(checked = repeatEnabled, onCheckedChange = { repeatEnabled = it })
                                AnimatedVisibility(visible = repeatEnabled) {
                                    RecurrenceControls(
                                        interval = recurrenceInterval,
                                        onIntervalChange = { recurrenceInterval = it },
                                        unit = recurrenceUnit,
                                        onUnitChange = { recurrenceUnit = it },
                                        type = recurrenceType,
                                        onTypeChange = { recurrenceType = it }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        if (showDueDatePicker) {
            val selectableDates = remember {
                object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        val candidateDate = Instant.fromEpochMilliseconds(utcTimeMillis).toLocalDateTime(TimeZone.UTC).date
                        return candidateDate >= Clock.System.todayIn(TimeZone.currentSystemDefault())
                    }
                }
            }
            val datePickerState = rememberDatePickerState(
                // Defaults to today rather than leaving nothing selected - opening the picker and
                // immediately hitting OK should just work.
                initialSelectedDateMillis = (dueDate ?: Clock.System.todayIn(TimeZone.currentSystemDefault()))
                    .atStartOfDayIn(TimeZone.UTC)
                    .toEpochMilliseconds(),
                selectableDates = selectableDates
            )
            AppDatePickerDialog(
                state = datePickerState,
                onDismissRequest = { showDueDatePicker = false },
                confirmButton = {
                    AppTextButton(emphasized = true, onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val pickedDate = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date
                            dueDate = pickedDate
                            // Today defaults to a reminder shortly from now, unless the time
                            // already picked is still later than that. Can roll over to tomorrow
                            // right before midnight.
                            val earliest = earliestReminder()
                            val currentTime = dueTime
                            if (pickedDate <= earliest.date &&
                                (currentTime == null || LocalDateTime(pickedDate, currentTime) < earliest)
                            ) {
                                dueDate = earliest.date
                                dueTime = earliest.time
                            }
                        }
                        showDueDatePicker = false
                    }) {
                        Text(stringResource(Res.string.form_ok))
                    }
                },
                dismissButton = {
                    AppTextButton(onClick = { showDueDatePicker = false }) {
                        Text(stringResource(Res.string.form_cancel))
                    }
                }
            )
        }
        // Asked up front when "Exact time" is ticked without the permission. Backing out of it
        // unticks the box again; the inline description and Grant permission button below stay
        // as the way back in.
        if (showExactPermissionDialog && exactAlarmPermission != null) {
            val cancel = {
                exactTimeEnabled = false
                showExactPermissionDialog = false
            }
            AppAlertDialog(
                onDismissRequest = cancel,
                title = { Text(stringResource(Res.string.form_exact_permission_title)) },
                text = { Text(stringResource(Res.string.form_exact_needs_permission)) },
                confirmButton = {
                    AppButton(onClick = {
                        showExactPermissionDialog = false
                        exactAlarmPermission.request()
                    }) {
                        Text(stringResource(Res.string.form_grant_permission))
                    }
                },
                dismissButton = {
                    AppTextButton(onClick = cancel) {
                        Text(stringResource(Res.string.form_cancel))
                    }
                }
            )
        }
        if (showDueTimePicker) {
            val timePickerState = rememberTimePickerState(
                initialHour = dueTime?.hour ?: 10,
                initialMinute = dueTime?.minute ?: 0,
                is24Hour = true
            )
            val pickedTime = LocalTime(timePickerState.hour, timePickerState.minute)
            val pickedDate = dueDate
            AppTimePickerDialog(
                state = timePickerState,
                onDismissRequest = { showDueTimePicker = false },
                confirmButton = {
                    AppTextButton(
                        onClick = {
                            dueTime = pickedTime
                            showDueTimePicker = false
                        },
                        // No past reminders - only checkable once a date is picked; picking
                        // today afterwards bumps an already-past time instead (see above).
                        enabled = pickedDate == null || LocalDateTime(pickedDate, pickedTime) >= localNow(),
                        emphasized = true
                    ) {
                        Text(stringResource(Res.string.form_ok))
                    }
                },
                dismissButton = {
                    AppTextButton(onClick = { showDueTimePicker = false }) {
                        Text(stringResource(Res.string.form_cancel))
                    }
                }
            )
        }
        // Without an open Reminder card, Repeat stands on its own - see the Reminder card above for
        // where it lives otherwise.
        if (showRecurrence) {
            val standaloneRepeat = !(showDueTime && dueTimeEnabled)
            AnimatedVisibility(visible = standaloneRepeat) {
                Column {
                    Spacer(Modifier.height(AppSpacing.xs))
                    RepeatToggleRow(checked = repeatEnabled, onCheckedChange = { repeatEnabled = it })
                }
            }
            AnimatedVisibility(visible = standaloneRepeat && repeatEnabled) {
                Column {
                    Spacer(Modifier.height(AppSpacing.s))
                    // A real bordered section for just the recurrence controls - not the Repeat
                    // checkbox above, which toggles the section rather than belonging inside it.
                    AppOutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        footer = { FormDescription(recurrenceDraft.describe()) }
                    ) {
                        RecurrenceControls(
                            interval = recurrenceInterval,
                            onIntervalChange = { recurrenceInterval = it },
                            unit = recurrenceUnit,
                            onUnitChange = { recurrenceUnit = it },
                            type = recurrenceType,
                            onTypeChange = { recurrenceType = it },
                            modifier = Modifier.padding(AppSpacing.m)
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(actionsTopSpacing))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            AppTextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.form_discard))
            }
            Spacer(Modifier.width(AppSpacing.s))
            AppButton(
                onClick = submit,
                enabled = canSubmit
            ) {
                Text(stringResource(if (editingItem != null) Res.string.form_save else Res.string.form_create))
            }
        }
    }
}

@Composable
private fun RepeatToggleRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Checkbox
            )
            .padding(vertical = AppSpacing.s),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppCheckToggle(
            checked = checked,
            onCheckedChange = null
        )
        Spacer(Modifier.width(AppSpacing.m))
        Text(stringResource(Res.string.form_repeat))
    }
}

// The recurrence controls themselves - hosted in their own bordered card when Repeat stands alone,
// or inside the Reminder card when both are on.
@Composable
private fun RecurrenceControls(
    interval: Int,
    onIntervalChange: (Int) -> Unit,
    unit: RecurrenceUnit,
    onUnitChange: (RecurrenceUnit) -> Unit,
    type: RecurrenceType,
    onTypeChange: (RecurrenceType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(Res.string.form_every))
            Spacer(Modifier.width(AppSpacing.s))
            AppCounter(
                value = interval,
                onDecrease = { onIntervalChange(interval - 1) },
                onIncrease = { onIntervalChange(interval + 1) },
                decreaseDescription = stringResource(Res.string.form_decrease),
                increaseDescription = stringResource(Res.string.form_increase),
                decreaseEnabled = interval > MIN_RECURRENCE_INTERVAL
            )
        }
        Spacer(Modifier.height(AppSpacing.s))
        AppSegmentedButtonRow(
            options = RecurrenceUnit.entries,
            selectedOption = unit,
            onOptionSelected = onUnitChange,
            label = resourceLabels(RecurrenceUnit.entries) { it.displayName() },
            modifier = Modifier.fillMaxWidth()
        )
        // Padding lives on the row itself (rather than a spacedBy gap on the Column) so the
        // toggle's clickable area grows with it - the visual spacing above/below stays 8dp.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = type == RecurrenceType.AfterCompletion,
                    onValueChange = { afterCompletion ->
                        onTypeChange(if (afterCompletion) RecurrenceType.AfterCompletion else RecurrenceType.Every)
                    },
                    role = Role.Checkbox
                )
                .padding(vertical = AppSpacing.s),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppCheckToggle(
                checked = type == RecurrenceType.AfterCompletion,
                onCheckedChange = null
            )
            Spacer(Modifier.width(AppSpacing.m))
            Text(stringResource(Res.string.form_only_after_completion))
        }
    }
}

// A note in a card's footer (see AppOutlinedCard) - what a section's settings will do, or what's
// stopping them. The footer itself carries the background and resizes as the text changes.
@Composable
private fun FormDescription(text: String) {
    Text(
        text = text,
        style = appSupportingTextStyle(),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

// A minimum of 1 - going lower would either mean "never" or need a negative-interval concept
// neither Recurrence nor its UI support.
private const val MIN_RECURRENCE_INTERVAL = 1


private val REMINDER_LEAD_TIME = 10.minutes

private fun localNow(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

// Now + REMINDER_LEAD_TIME, truncated to the minute - the pickers have no seconds.
private fun earliestReminder(): LocalDateTime {
    val earliest = (Clock.System.now() + REMINDER_LEAD_TIME).toLocalDateTime(TimeZone.currentSystemDefault())
    return LocalDateTime(earliest.date, LocalTime(earliest.hour, earliest.minute))
}

// A read-only OutlinedTextField that opens a picker on tap - the field itself exposes no onClick,
// so a transparent clickable Box on top intercepts the tap instead (readOnly alone would still let
// the field take focus without doing anything useful).
@Composable
private fun ReadOnlyPickerField(
    value: String,
    label: String,
    placeholder: String,
    icon: ImageVector,
    iconContentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // The whole field is one click target (the overlay below), so the trailing icon doesn't
        // need its own 48dp touch area - dropping it lets the icon's padding shrink with the field's.
        // The icon is then pinned to the field's edge, so it carries its own end padding; the gap
        // on its start side already comes from the field's end padding.
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
            AppTextField(
                state = remember(value) { TextFieldState(value) },
                readOnly = true,
                label = { Text(label) },
                placeholder = { Text(placeholder) },
                trailingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = iconContentDescription,
                        modifier = Modifier.padding(end = AppSpacing.s)
                    )
                },
                lineLimits = TextFieldLineLimits.SingleLine,
                compact = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
        )
    }
}

// The count+unit phrase ("2 weeks") is pluralized on its own via a real plurals resource - not
// just an English-only "add an s" rule - then dropped into one of the two sentence shapes below.
// Real (locale-aware, CLDR one/few/many/other) plural resources work identically across every
// Compose Multiplatform target, not just Android.
@Composable
private fun Recurrence.describe(): String {
    val unitPhrase = pluralStringResource(unit.pluralResource(), interval, interval)
    return when (type) {
        RecurrenceType.Every -> stringResource(Res.string.recurrence_every, unitPhrase)
        RecurrenceType.AfterCompletion -> stringResource(Res.string.recurrence_after_completion, unitPhrase)
    }
}

// The Reminder + Repeat card's single description: when the first reminder fires and how the
// recurrence carries it forward - Every keeps reminding on schedule, AfterCompletion only once the
// todo is completed again (see reachedDueOccurrence/ToggleTodoDone).
@Composable
private fun Recurrence.describeWithReminder(date: String, time: String): String {
    val unitPhrase = pluralStringResource(unit.pluralResource(), interval, interval)
    return when (type) {
        RecurrenceType.Every -> stringResource(Res.string.recurrence_every_with_reminder, date, time, unitPhrase)
        RecurrenceType.AfterCompletion -> stringResource(Res.string.recurrence_after_completion_with_reminder, date, time, unitPhrase)
    }
}

private fun RecurrenceUnit.pluralResource(): PluralStringResource = when (this) {
    RecurrenceUnit.Day -> Res.plurals.recurrence_unit_day
    RecurrenceUnit.Week -> Res.plurals.recurrence_unit_week
    RecurrenceUnit.Month -> Res.plurals.recurrence_unit_month
    RecurrenceUnit.Year -> Res.plurals.recurrence_unit_year
}

private fun RecurrenceUnit.displayName(): StringResource = when (this) {
    RecurrenceUnit.Day -> Res.string.recurrence_day
    RecurrenceUnit.Week -> Res.string.recurrence_week
    RecurrenceUnit.Month -> Res.string.recurrence_month
    RecurrenceUnit.Year -> Res.string.recurrence_year
}

@Preview
@Composable
private fun TodoFormBottomSheetPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        TodoFormBottomSheet(
            editingItem = null,
            onConfirm = {},
            onDismiss = {},
            onMoreSettingsClick = {}
        )
    }
}
