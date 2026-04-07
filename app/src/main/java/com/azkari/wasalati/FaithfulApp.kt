package com.azkari.wasalati

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun AzkariFaithfulApp(
    viewModel: FaithfulMainViewModel,
    notificationPermissionGranted: Boolean,
    exactAlarmPermissionGranted: Boolean,
    onRequestNotifications: () -> Unit,
    onRequestExactAlarms: () -> Unit,
    onUseCurrentLocation: () -> Unit,
) {
    val uiState = viewModel.uiState

    AzkariFaithfulTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(ForestLight.copy(alpha = 0.08f), Cream),
                            radius = 1400f,
                        ),
                    )
                    .testTag("app_root"),
            ) {
                Scaffold(
                    containerColor = Color.Transparent,
                    topBar = {
                        HomeStickyHeader(
                            uiState = uiState,
                            onOpenSettings = { viewModel.openModal(AppModal.SETTINGS) },
                            onOpenQada = { viewModel.openModal(AppModal.QADA) },
                            onOpenCity = { viewModel.openModal(AppModal.CITY) },
                            onToggleBanner = viewModel::toggleBanner,
                            onOpenTracker = { viewModel.openModal(AppModal.TRACKER) },
                            onOpenQuran = viewModel::openQuran,
                            onSelectTab = viewModel::selectTab,
                        )
                    },
                ) { padding ->
                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = Forest)
                        }
                    } else {
                        HomeContent(
                            modifier = Modifier.padding(padding),
                            uiState = uiState,
                            viewModel = viewModel,
                        )
                    }
                }

                if (uiState.isOffline) {
                    OfflineBanner(Modifier.align(Alignment.TopCenter).padding(top = 92.dp))
                }

                uiState.currentToast?.let { toast ->
                    ToastCard(
                        message = toast.message,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .testTag("toast_card"),
                    )
                }

                FaithfulModalHost(
                    uiState = uiState,
                    viewModel = viewModel,
                    notificationPermissionGranted = notificationPermissionGranted,
                    exactAlarmPermissionGranted = exactAlarmPermissionGranted,
                    onRequestNotifications = onRequestNotifications,
                    onRequestExactAlarms = onRequestExactAlarms,
                    onUseCurrentLocation = onUseCurrentLocation,
                )
            }
        }
    }
}

@Composable
private fun AppHeader(
    uiState: AppUiState2,
    onOpenSettings: () -> Unit,
    onOpenQada: () -> Unit,
    onOpenCity: () -> Unit,
) {
    Surface(
        color = Color.White.copy(alpha = 0.96f),
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BrandMark()
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("أذكاري وصلاتي", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                Text(
                    text = uiState.hijriDate,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            CircleIconButton(onClick = onOpenSettings, icon = Icons.Rounded.Tune, tag = "settings_button")
            Spacer(Modifier.width(8.dp))
            Box {
                CircleIconButton(onClick = onOpenQada, icon = Icons.Rounded.AccountBalance, tint = Color(0xFFE11D48), tag = "qada_button")
                if (uiState.qada.values.sum() > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .clip(CircleShape)
                            .background(Color(0xFFE11D48))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(uiState.qada.values.sum().toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFFFF5F5),
                modifier = Modifier.clickable(onClick = onOpenCity),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = uiState.currentCity?.name ?: "اختر مدينة",
                        fontSize = 12.sp,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeStickyHeader(
    uiState: AppUiState2,
    onOpenSettings: () -> Unit,
    onOpenQada: () -> Unit,
    onOpenCity: () -> Unit,
    onToggleBanner: () -> Unit,
    onOpenTracker: () -> Unit,
    onOpenQuran: () -> Unit,
    onSelectTab: (HomeTab) -> Unit,
) {
    Surface(
        color = Color.White.copy(alpha = 0.96f),
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BrandMark()
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("أذكاري وصلاتي", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    Text(
                        text = uiState.hijriDate,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                CircleIconButton(onClick = onOpenSettings, icon = Icons.Rounded.Tune, tag = "settings_button")
                Spacer(Modifier.width(8.dp))
                Box {
                    CircleIconButton(onClick = onOpenQada, icon = Icons.Rounded.AccountBalance, tint = Color(0xFFE11D48), tag = "qada_button")
                    if (uiState.qada.values.sum() > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .clip(CircleShape)
                                .background(Color(0xFFE11D48))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(uiState.qada.values.sum().toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFFFF5F5),
                    modifier = Modifier.clickable(onClick = onOpenCity),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = uiState.currentCity?.name ?: "اختر مدينة",
                            fontSize = 12.sp,
                            maxLines = 1,
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            if (uiState.currentCity != null && uiState.prayerSummary != null) {
                PrayerBanner(
                    uiState = uiState,
                    onToggle = onToggleBanner,
                    onOpenTracker = onOpenTracker,
                )
                Spacer(Modifier.height(6.dp))
            }

            QuranEntryCard(onOpenQuran = onOpenQuran)

            if (uiState.quran.dailyGoal > 0) {
                Spacer(Modifier.height(10.dp))
                QuranDailyTrackerCard(uiState.quran)
            }

            Spacer(Modifier.height(10.dp))
            TabStrip(selected = uiState.selectedTab, onSelect = onSelectTab)
        }
    }
}

@Composable
private fun BrandMark() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(ForestDark, Forest))),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.AccountBalance, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun CircleIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color = Forest,
    tag: String,
) {
    Surface(
        shape = CircleShape,
        color = Color(0xFFF5F5F4),
        modifier = Modifier.testTag(tag),
    ) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = null, tint = tint)
        }
    }
}

@Composable
private fun HomeContent(
    modifier: Modifier,
    uiState: AppUiState2,
    viewModel: FaithfulMainViewModel,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        var previousScroll = 0
        snapshotFlow {
            (listState.firstVisibleItemIndex * 10_000) + listState.firstVisibleItemScrollOffset
        }
            .distinctUntilChanged()
            .collectLatest { currentScroll: Int ->
                val delta = currentScroll - previousScroll
                when {
                    currentScroll <= 8 -> viewModel.setBannerCollapsed(false)
                    delta > 18 -> viewModel.setBannerCollapsed(true)
                }

                if (delta > 2 || delta < -2) {
                    previousScroll = currentScroll
                }
            }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_list"),
        state = listState,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            if (uiState.currentCity == null || uiState.currentPrayerTimes == null || uiState.prayerSummary == null) {
                EmptyCityCard(onOpenCity = { viewModel.openModal(AppModal.CITY) })
            }
        }

        uiState.suggestion?.let { suggestion ->
            item {
                FaithfulSuggestionCard(
                    suggestion = suggestion,
                    onReset = viewModel::resetAutoTab,
                )
            }
        }

        if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            item {
                FridayKahfCard(onOpen = viewModel::openSurahKahf)
            }
        }

        if (uiState.homeSections.isEmpty()) {
            item {
                EmptyStateCard()
            }
        } else {
            uiState.homeSections.forEach { section ->
                if (section.title != null) {
                    item(key = "${section.id}_divider") {
                        SectionDivider(section)
                    }
                }
                section.items.forEachIndexed { index, item ->
                    item(key = "${section.id}_$index") {
                        AzkarItemCard(
                            section = section,
                            index = index,
                            item = item,
                            remaining = viewModel.itemRemaining["${section.id}:$index"] ?: item.count,
                            onCount = { viewModel.decrementAzkar(section.id, index, item.count) },
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "﷽ — صدقة جارية للحاج ماهر كرم وجميع أموات المسلمين رحمهم الله",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun EmptyCityCard(onOpenCity: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().testTag("home_empty_state"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = Forest, modifier = Modifier.size(42.dp))
            Spacer(Modifier.height(10.dp))
            Text("ابدأ باختيار مدينتك", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "اختر مدينتك لعرض مواقيت الصلاة والأذكار المناسبة.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(14.dp))
            OutlinedButton(onClick = onOpenCity) {
                Icon(Icons.Rounded.LocationOn, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("اختيار مدينة")
            }
        }
    }
}

@Composable
private fun PrayerBanner(
    uiState: AppUiState2,
    onToggle: () -> Unit,
    onOpenTracker: () -> Unit,
) {
    val summary = uiState.prayerSummary ?: return
    val shape = RoundedCornerShape(if (uiState.bannerCollapsed) 40.dp else 20.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .border(1.dp, Color.White.copy(alpha = 0.08f), shape)
            .testTag("prayer_banner"),
        shape = shape,
        color = Color.Transparent,
        shadowElevation = if (uiState.bannerCollapsed) 3.dp else 12.dp,
    ) {
        Box(
            modifier = Modifier.background(
                Brush.linearGradient(
                    listOf(Color(0xFF0A1628), ForestDark, Color(0xFF064E3B)),
                ),
            ),
        ) {
            Column {
                if (uiState.bannerCollapsed) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onToggle)
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(1.dp),
                        ) {
                            Text(
                                text = "المتبقي للصلاة القادمة",
                                color = ForestLight.copy(alpha = 0.78f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = formatCountdown(summary.countdownMillis),
                                style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    letterSpacing = 1.1.sp,
                                    color = Color.White,
                                ),
                            )
                            Text(
                                text = summary.nextPrayer.displayName(),
                                color = Color(0xFFC5F4D8),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 1.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height(2.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.12f)),
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onToggle)
                            .padding(horizontal = 11.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "المتبقي للصلاة القادمة",
                                color = ForestLight.copy(alpha = 0.7f),
                                fontSize = 7.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = formatCountdown(summary.countdownMillis),
                                    style = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        letterSpacing = 1.sp,
                                        color = Color.White,
                                    ),
                                )
                                Spacer(Modifier.width(5.dp))
                                Row(
                                    modifier = Modifier.padding(bottom = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text("←", color = Color.White.copy(alpha = 0.55f), fontSize = 8.sp)
                                    Spacer(Modifier.width(2.dp))
                                    Text(
                                        summary.nextPrayer.displayName(),
                                        color = ForestLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.sp,
                                    )
                                }
                            }
                        }
                        PrayerDotsRow(uiState.prayerDots)
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Rounded.KeyboardArrowUp,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(12.dp),
                            )
                        }
                    }
                }

                AnimatedVisibility(!uiState.bannerCollapsed) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        SunnahStrip(summary.sunnahInfo)
                        PrayerGrid(uiState.prayerDots)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "متابعة الصلوات",
                                color = ForestLight,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable(onClick = onOpenTracker),
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = "${uiState.todayDoneCount}/5 اليوم",
                                color = Color.White.copy(alpha = 0.38f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                            )
                            Spacer(Modifier.width(8.dp))
                            if (uiState.streakDays >= 2) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFF97316))))
                                        .padding(horizontal = 7.dp, vertical = 2.dp),
                                ) {
                                    Text(
                                        text = "🔥 ${uiState.streakDays}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrayerDotsRow(dots: List<PrayerDotUi>) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        dots.forEach { dot ->
            val (bg, border) = when (dot.status) {
                PrayerDotStatus.DONE -> ForestLight to ForestLight
                PrayerDotStatus.MISSED -> Color(0xFFF43F5E) to Color(0xFFF43F5E)
                PrayerDotStatus.PENDING -> Color.White.copy(alpha = 0.13f) to Color.White.copy(alpha = 0.18f)
                PrayerDotStatus.UPCOMING -> Color.White.copy(alpha = 0.04f) to Color.White.copy(alpha = 0.07f)
            }
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .background(bg, CircleShape)
                    .border(1.5.dp, border, CircleShape),
            )
        }
    }
}

@Composable
private fun SunnahStrip(sunnahInfo: SunnahInfo) {
    val shape = RoundedCornerShape(18.dp)
    Surface(shape = shape, color = Color.Transparent, modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.09f), shape)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.05f))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("قبل", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                Text(sunnahInfo.pre, color = Color.White, textAlign = TextAlign.Center, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("بعد", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                Text(sunnahInfo.post, color = Color.White, textAlign = TextAlign.Center, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun PrayerGrid(dots: List<PrayerDotUi>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        dots.forEach { item ->
            val alpha = if (item.status == PrayerDotStatus.UPCOMING) 1f else 0.55f
            Surface(
                modifier = Modifier.weight(1f).alpha(alpha),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.08f),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(item.label, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                    Text(item.timeLabel, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    PrayerDotsRow(listOf(item))
                }
            }
        }
    }
}

@Composable
private fun QuranEntryCard(onOpenQuran: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    val borderColor = GoldBright.copy(alpha = 0.25f)
    Surface(shape = shape, color = Color.Transparent, modifier = Modifier.fillMaxWidth().testTag("quran_entry_card")) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        listOf(GoldBright.copy(alpha = 0.09f), GoldBright.copy(alpha = 0.02f)),
                    ),
                )
                .border(1.dp, borderColor, shape)
                .clickable(onClick = onOpenQuran)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.8f))
                        .border(1.dp, Color(0x80FFEDD5), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("📖", fontSize = 14.sp, color = GoldBright)
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "القرآن الكريم",
                    style = TextStyle(fontFamily = AmiriFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AmberText),
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.alpha(0.7f)) {
                Text(
                    text = "قراءة واستماع",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFA38A64),
                )
                Icon(Icons.Rounded.KeyboardArrowLeft, contentDescription = null, tint = Color(0xFFA38A64), modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun QuranDailyTrackerCard(quranUiState: QuranUiState) {
    val goal = quranUiState.dailyGoal.coerceAtLeast(1)
    val progress = (quranUiState.dailyLog.pagesRead.size.toFloat() / goal).coerceIn(0f, 1f)
    val shape = RoundedCornerShape(20.dp)
    Surface(
        shape = shape,
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GoldBright.copy(alpha = 0.15f), shape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = GoldBright.copy(alpha = 0.45f), modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(8.dp))
            Text("وِردك اليومي", modifier = Modifier.weight(1f), color = Color(0xFF8B7355), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(
                text = "${quranUiState.dailyLog.pagesRead.size}/${quranUiState.dailyGoal}",
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB48530),
                fontSize = 12.sp,
            )
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(GoldBright.copy(alpha = 0.15f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Brush.horizontalGradient(listOf(GoldBright, Color(0xFFB48530)))),
                )
            }
            if (quranUiState.stats.streakDays >= 2) {
                Spacer(Modifier.width(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔥", fontSize = 12.sp, color = Color(0xFFF97316))
                    Spacer(Modifier.width(4.dp))
                    Text(quranUiState.stats.streakDays.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB48530))
                }
            }
        }
    }
}

@Composable
private fun TabStrip(selected: HomeTab, onSelect: (HomeTab) -> Unit) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .testTag("tab_strip"),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        HomeTab.entries.filter { it != HomeTab.FRIDAY || Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY }
            .forEach { tab ->
                HomeTabPill(
                    tab = tab,
                    selected = selected == tab,
                    onClick = { onSelect(tab) },
                )
            }
    }
}

@Composable
private fun HomeTabPill(
    tab: HomeTab,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val isFriday = tab == HomeTab.FRIDAY
    val shape = RoundedCornerShape(20.dp)
    val background = when {
        selected -> Forest
        isFriday -> Color(0xFFFFFBEB).copy(alpha = 0.7f)
        else -> Color.White.copy(alpha = 0.7f)
    }
    val border = when {
        selected -> Color.Transparent
        isFriday -> Color(0xFFFEF3C7).copy(alpha = 0.8f)
        else -> Color(0xFFF3F4F6).copy(alpha = 0.8f)
    }
    val labelColor = when {
        selected -> Color.White
        isFriday -> Color(0xFFB45309)
        else -> Color(0xFF6B7280)
    }
    Surface(
        shape = shape,
        color = background,
        shadowElevation = if (selected) 3.dp else 0.dp,
        modifier = Modifier
            .border(1.dp, border, shape)
            .clickable(onClick = onClick),
    ) {
        Text(
            tab.chipLabel,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = labelColor,
            maxLines = 1,
        )
    }
}

@Composable
private fun FaithfulSuggestionCard(
    suggestion: String,
    onReset: () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    Surface(
        shape = shape,
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Sage, shape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFECFDF5).copy(alpha = 0.8f))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("⏱️", fontSize = 14.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                suggestion,
                modifier = Modifier.weight(1f),
                color = Color(0xFF065F46),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.Transparent,
                modifier = Modifier
                    .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(10.dp))
                    .clickable(onClick = onReset),
            ) {
                Text("تلقائي", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestDark)
            }
        }
    }
}

@Composable
private fun FridayKahfCard(onOpen: () -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Surface(shape = shape, color = Color.Transparent, modifier = Modifier.fillMaxWidth().border(1.dp, Color(0x38FBBF24), shape), shadowElevation = 4.dp) {
        Row(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(Color(0xFF422006), Color(0xFF78350F), Color(0xFF92400E))))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x33FBBF24)),
                contentAlignment = Alignment.Center,
            ) {
                Text("🕌", fontSize = 18.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("يوم الجمعة المبارك", color = Color(0xFFFBBF24), fontWeight = FontWeight.ExtraBold, style = TextStyle(fontFamily = AmiriFamily))
                Text("«من قرأ سورة الكهف يوم الجمعة أضاء له النور ما بين الجمعتين»", color = Color(0xBFFDE68A), fontSize = 10.sp, lineHeight = 16.sp)
            }
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0x2EFBBF24),
                modifier = Modifier
                    .border(1.dp, Color(0x4DFBBF24), RoundedCornerShape(14.dp))
                    .clickable(onClick = onOpen),
            ) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("📖", fontSize = 12.sp)
                    Spacer(Modifier.width(6.dp))
                    Text("اقرأ الكهف", color = Color(0xFFFBBF24), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun SectionDivider(section: AzkarSection) {
    val colors = when (section.palette) {
        SectionPalette.WAKING -> listOf(Forest, Color(0xFF047857))
        SectionPalette.MORNING -> listOf(Color(0xFF0F766E), Color(0xFF0D9488))
        SectionPalette.DUHA -> listOf(Color(0xFFB45309), Color(0xFFD97706))
        SectionPalette.EVENING -> listOf(Color(0xFF1E3A5F), Color(0xFF1E40AF))
        SectionPalette.SLEEP -> listOf(Color(0xFF2D1B69), Color(0xFF4C1D95))
        SectionPalette.TAHAJJUD -> listOf(Color(0xFF1E1B4B), Color(0xFF312E81))
        SectionPalette.FRIDAY -> listOf(Color(0xFF854D0E), Color(0xFF92400E))
        SectionPalette.PRAYER -> listOf(ForestDark, Forest)
        SectionPalette.PLAIN -> listOf(Forest, ForestMid)
    }
    Surface(shape = RoundedCornerShape(20.dp), color = Color.Transparent) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(colors))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(section.icon.orEmpty())
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(section.title.orEmpty(), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                section.subtitle?.let {
                    Text(it, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun AzkarItemCard(
    section: AzkarSection,
    index: Int,
    item: AzkarItem,
    remaining: Int,
    onCount: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = Color(0xFFFFFCF7),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .border(1.dp, Color(0x17065F46), RoundedCornerShape(26.dp))
            .testTag("azkar_card_${section.id}_$index"),
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFFFCF7), Color(0xFFF7FBF8)),
                    ),
                )
                .padding(horizontal = 18.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item.title?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Sage.copy(alpha = 0.82f),
                    ) {
                        Text(
                            text = it,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            color = Forest,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                        )
                    }
                }
            }

            Text(
                text = item.text,
                style = if (item.isQuran) {
                    TextStyle(fontFamily = AmiriFamily, fontSize = 22.sp, lineHeight = 44.sp, color = ForestDark)
                } else {
                    MaterialTheme.typography.bodyLarge.copy(
                        color = Color(0xFF1C2430),
                        fontSize = 18.sp,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                textAlign = TextAlign.Center,
            )

            CountBubble(
                remaining = remaining,
                total = item.count,
                onCount = onCount,
            )

            item.fadl?.let {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFFFF2D8),
                ) {
                    Text(
                        text = "فضل: $it",
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xFF8D5E10),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun CountBubble(
    remaining: Int,
    total: Int,
    onCount: () -> Unit,
) {
    val progress = if (total <= 0) 1f else 1f - (remaining.toFloat() / total.toFloat())
    val complete = remaining == 0
    val shape = RoundedCornerShape(18.dp)
    Surface(
        shape = shape,
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = remaining > 0, onClick = onCount),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(
                    if (complete) {
                        Brush.linearGradient(listOf(Color(0xFF0A7555), ForestDark))
                    } else {
                        Brush.verticalGradient(listOf(Color(0xFFFCFFFD), Color(0xFFEAF7F1)))
                    },
                )
                .border(
                    width = 1.dp,
                    color = if (complete) Forest else Color(0x29065F46),
                    shape = shape,
                )
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (complete) "اكتمل الذكر" else "اضغط للعد",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (complete) Color.White.copy(alpha = 0.86f) else ForestMid,
            )

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = if (complete) "تم" else remaining.toString(),
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (complete) Color.White else ForestDark,
                )
                if (total > 1) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "/$total",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (complete) Color.White.copy(alpha = 0.72f) else Color(0xFF7F8F89),
                        modifier = Modifier.padding(bottom = 2.dp),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (complete) Color.White.copy(alpha = 0.18f) else Color.White),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(4.dp)
                        .background(
                            if (complete) {
                                Brush.horizontalGradient(listOf(GoldLight, GoldLight))
                            } else {
                                Brush.horizontalGradient(listOf(ForestLight, Forest))
                            },
                        ),
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard() {
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(Icons.Rounded.AccountBalance, contentDescription = null, tint = Color(0xFFD6D3D1), modifier = Modifier.size(52.dp))
            Spacer(Modifier.height(10.dp))
            Text("لا توجد عناصر لعرضها الآن", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun OfflineBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = ForestDark,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("وضع عدم الاتصال — التطبيق يعمل بالكامل", color = Color.White, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun ToastCard(
    message: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 12.dp,
        color = ForestDark,
    ) {
        Text(
            text = message,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun formatCountdown(millis: Long): String {
    val totalSeconds = (millis / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}
