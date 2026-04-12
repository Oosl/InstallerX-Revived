package com.rosan.installer.ui.page.main.widget.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rosan.installer.R
import com.rosan.installer.domain.settings.model.ConfigModel
import com.rosan.installer.ui.icons.AppIcons
import com.rosan.installer.ui.page.main.settings.config.all.AllViewAction
import com.rosan.installer.ui.page.main.settings.config.all.AllViewModel
import com.rosan.installer.ui.page.main.widget.chip.CapsuleTag
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop

@Composable
fun ShowDataWidget(
    viewModel: AllViewModel,
    listState: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    backdrop: LayerBackdrop? = null,
    adaptiveMinSize: Dp = 320.dp
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val configs = uiState.data.configs
    val minId = configs.minByOrNull { it.id }?.id

    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxSize()
            .then(backdrop?.let { Modifier.layerBackdrop(it) } ?: Modifier),
        columns = GridCells.Adaptive(adaptiveMinSize),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        state = listState,
    ) {
        // Insert the tip card as a list item to match MIUIX behavior
        if (!uiState.userReadScopeTips)
            item {
                ScopeTipCard(viewModel = viewModel)
            }

        items(configs) { entity ->
            DataItemWidget(
                viewModel = viewModel,
                entity = entity,
                isDefault = entity.id == minId
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DataItemWidget(
    viewModel: AllViewModel,
    entity: ConfigModel,
    isDefault: Boolean
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceBright)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = entity.name,
                        style = MaterialTheme.typography.titleMediumEmphasized
                    )
                    if (isDefault) {
                        Spacer(modifier = Modifier.size(8.dp))
                        CapsuleTag(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            text = stringResource(R.string.config_global_default),
                        )
                    } else if (entity.scopeCount == 0) {
                        Spacer(modifier = Modifier.size(8.dp))
                        // Display a warning tag for configurations that are not default and have no scopes applied
                        CapsuleTag(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            text = stringResource(R.string.config_status_inactive),
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                if (entity.description.isNotEmpty()) {
                    Text(
                        text = entity.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.outline
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.dispatch(AllViewAction.EditDataConfig(entity)) }) {
                    Icon(
                        imageVector = AppIcons.Edit,
                        contentDescription = stringResource(id = R.string.edit)
                    )
                }
                if (!isDefault)
                    IconButton(onClick = { viewModel.dispatch(AllViewAction.DeleteDataConfig(entity)) }) {
                        Icon(
                            imageVector = AppIcons.Delete,
                            contentDescription = stringResource(id = R.string.delete)
                        )
                    }
                IconButton(onClick = {
                    viewModel.dispatch(AllViewAction.ApplyConfig(entity))
                }) {
                    Icon(
                        imageVector = AppIcons.Rule,
                        contentDescription = stringResource(id = R.string.apply)
                    )
                }
            }
        }
    }
}