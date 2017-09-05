/*
 * Copyright (c) 2017. Jahir Fiquitiva
 *
 * Licensed under the CreativeCommons Attribution-ShareAlike
 * 4.0 International License. You may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *    http://creativecommons.org/licenses/by-sa/4.0/legalcode
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jahirfiquitiva.libs.blueprint.ui.activities

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.widget.TextViewCompat
import android.view.View
import android.widget.TextView
import ca.allanwang.kau.utils.gone
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import jahirfiquitiva.libs.blueprint.R
import jahirfiquitiva.libs.blueprint.data.models.NavigationItem
import jahirfiquitiva.libs.blueprint.helpers.utils.DEFAULT_CREDITS_POSITION
import jahirfiquitiva.libs.blueprint.helpers.utils.DEFAULT_HELP_POSITION
import jahirfiquitiva.libs.blueprint.helpers.utils.DEFAULT_SETTINGS_POSITION
import jahirfiquitiva.libs.blueprint.ui.activities.base.InternalBaseBlueprintActivity
import jahirfiquitiva.libs.kauextensions.extensions.accentColor
import jahirfiquitiva.libs.kauextensions.extensions.getAppName
import jahirfiquitiva.libs.kauextensions.extensions.getAppVersion
import jahirfiquitiva.libs.kauextensions.extensions.getBoolean
import jahirfiquitiva.libs.kauextensions.extensions.getDrawable

abstract class DrawerBlueprintActivity:InternalBaseBlueprintActivity() {
    
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        initDrawer(savedInstanceState)
    }
    
    override fun onSaveInstanceState(outState:Bundle?) {
        val nOutState = drawer?.saveInstanceState(outState)
        super.onSaveInstanceState(nOutState)
    }
    
    override fun onRestoreInstanceState(savedInstanceState:Bundle?) =
            super.onRestoreInstanceState(savedInstanceState)
    
    private fun initDrawer(savedInstance:Bundle?) {
        val v:View = findViewById(R.id.bottom_navigation)
        v.gone()
        val accountHeaderBuilder = AccountHeaderBuilder().withActivity(this)
        val header:Drawable? = "drawer_header".getDrawable(this)
        if (header != null) {
            accountHeaderBuilder.withHeaderBackground(header)
        } else {
            accountHeaderBuilder.withHeaderBackground(accentColor)
        }
        if (getBoolean(R.bool.with_drawer_texts)) {
            accountHeaderBuilder.withSelectionFirstLine(getAppName())
            accountHeaderBuilder.withSelectionSecondLine("v " + getAppVersion())
        }
        accountHeaderBuilder.withProfileImagesClickable(false)
                .withResetDrawerOnProfileListClick(false)
                .withSelectionListEnabled(false)
                .withSelectionListEnabledForSingleProfile(false)
        
        if (savedInstance != null)
            accountHeaderBuilder.withSavedInstance(savedInstance)
        
        val accountHeader = accountHeaderBuilder.build()
        
        val drawerTitle:TextView = accountHeader.view.findViewById(
                R.id.material_drawer_account_header_name)
        val drawerSubtitle:TextView = accountHeader.view.findViewById(
                R.id.material_drawer_account_header_email)
        
        TextViewCompat.setTextAppearance(drawerTitle, R.style.DrawerTextsWithShadow)
        TextViewCompat.setTextAppearance(drawerSubtitle, R.style.DrawerTextsWithShadow)
        
        val drawerBuilder = DrawerBuilder().withActivity(this)
        getToolbar()?.let {
            drawerBuilder.withToolbar(it)
        }
        drawerBuilder.withAccountHeader(accountHeader)
                .withDelayOnDrawerClose(-1)
                .withShowDrawerOnFirstLaunch(true)
        
        drawerBuilder.withOnDrawerItemClickListener { _, _, drawerItem ->
            try {
                when (drawerItem.identifier) {
                    DEFAULT_HELP_POSITION.toLong() -> {
                        drawer?.closeDrawer()
                        drawer?.setSelection(currentItemId.toLong(), false)
                        // TODO: Launch Help activity
                        return@withOnDrawerItemClickListener false
                    }
                    DEFAULT_CREDITS_POSITION.toLong() -> {
                        drawer?.closeDrawer()
                        drawer?.setSelection(currentItemId.toLong(), false)
                        startActivity(Intent(this, CreditsActivity::class.java))
                        return@withOnDrawerItemClickListener false
                    }
                    DEFAULT_SETTINGS_POSITION.toLong() -> {
                        drawer?.closeDrawer()
                        drawer?.setSelection(currentItemId.toLong(), false)
                        startActivity(Intent(this, SettingsActivity::class.java))
                        return@withOnDrawerItemClickListener false
                    }
                    else -> {
                        val navigated = navigateToItem(
                                getNavigationItems()[drawerItem.identifier.toInt()])
                        if (navigated) drawer?.closeDrawer()
                        return@withOnDrawerItemClickListener navigated
                    }
                }
            } catch (e:Exception) {
                e.printStackTrace()
            }
            return@withOnDrawerItemClickListener false
        }
        
        getNavigationItems().forEach {
            drawerBuilder.addDrawerItems(
                    PrimaryDrawerItem().withIdentifier(it.id.toLong())
                            .withName(it.title)
                            .withIcon(getDrawable(it.icon, null))
                            .withIconTintingEnabled(true))
        }
        
        drawerBuilder.addDrawerItems(DividerDrawerItem())
        
        drawerBuilder.addDrawerItems(SecondaryDrawerItem()
                                             .withIdentifier(DEFAULT_HELP_POSITION.toLong())
                                             .withName(R.string.section_help))
        
        drawerBuilder.addDrawerItems(SecondaryDrawerItem()
                                             .withIdentifier(DEFAULT_CREDITS_POSITION.toLong())
                                             .withName(R.string.section_about))
        
        drawerBuilder.addDrawerItems(SecondaryDrawerItem()
                                             .withIdentifier(DEFAULT_SETTINGS_POSITION.toLong())
                                             .withName(R.string.settings))
        
        drawerBuilder.withHasStableIds(true)
                .withFireOnInitialOnClick(true)
        
        if (savedInstance != null)
            drawerBuilder.withSavedInstance(savedInstance)
        
        drawer = drawerBuilder.build()
        
        updateToolbarColorsHere(0)
    }
    
    override fun onBackPressed() {
        val isOpen = drawer?.isDrawerOpen == true
        when {
            isOpen -> drawer?.closeDrawer()
            currentItemId != 0 -> navigateToItem(getNavigationItems()[0])
            else -> super.onBackPressed()
        }
    }
    
    override fun navigateToItem(item:NavigationItem):Boolean {
        val result = super.navigateToItem(item)
        if (result) drawer?.setSelection(item.id.toLong())
        return result
    }
    
    override fun hasBottomBar():Boolean = false
    
}