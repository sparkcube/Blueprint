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
 *
 * Special thanks to the project contributors and collaborators
 * 	https://github.com/jahirfiquitiva/Blueprint#special-thanks
 */

package jahirfiquitiva.libs.blueprint.activities

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.widget.TextViewCompat
import android.view.View
import android.widget.TextView
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import jahirfiquitiva.libs.blueprint.R
import jahirfiquitiva.libs.blueprint.activities.base.InternalBaseBlueprintActivity
import jahirfiquitiva.libs.blueprint.extensions.*

abstract class DrawerBlueprintActivity:InternalBaseBlueprintActivity() {

    private lateinit var drawer:Drawer

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        initDrawer(savedInstanceState)
    }

    override fun onSaveInstanceState(outState:Bundle?) {
        val nOutState = drawer.saveInstanceState(outState)
        super.onSaveInstanceState(nOutState)
    }

    override fun onRestoreInstanceState(savedInstanceState:Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
    }

    fun initDrawer(savedInstance:Bundle?) {
        val v:View = findViewById(R.id.bottom_navigation)
        v.makeGone()
        val accountHeaderBuilder = AccountHeaderBuilder().withActivity(this)
        val header:Drawable? = "drawer_header".getDrawable(this)
        if (header != null) {
            accountHeaderBuilder.withHeaderBackground(header)
        } else {
            accountHeaderBuilder.withHeaderBackground(getAccentColor(isDarkTheme()))
        }
        if (getBoolean(R.bool.with_drawer_texts)) {
            accountHeaderBuilder.withSelectionFirstLine(getString(R.string.app_long_name))
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
        if (getToolbar() != null) drawerBuilder.withToolbar(getToolbar()!!)
        drawerBuilder.withAccountHeader(accountHeader)
                .withDelayOnDrawerClose(-1)
                .withShowDrawerOnFirstLaunch(true)

        drawerBuilder.withOnDrawerItemClickListener { _, _, drawerItem ->
            try {
                val navigated = navigateToItem(getNavigationItems()[drawerItem.identifier.toInt()])
                if (navigated) drawer.closeDrawer()
                return@withOnDrawerItemClickListener navigated
            } catch (ignored:Exception) {
                return@withOnDrawerItemClickListener true
            }
        }

        getNavigationItems().forEach {
            drawerBuilder.addDrawerItems(
                    PrimaryDrawerItem().withIdentifier(it.id.toLong())
                            .withName(it.title)
                            .withIcon(getDrawable(it.icon, null))
                            .withIconTintingEnabled(true))
        }

        drawerBuilder.withHasStableIds(true)
                .withShowDrawerUntilDraggedOpened(true)
                .withFireOnInitialOnClick(true)

        if (savedInstance != null)
            drawerBuilder.withSavedInstance(savedInstance)

        drawer = drawerBuilder.build()
    }

    override fun onBackPressed() {
        if (currentItemId != 0) navigateToItem(getNavigationItems()[0])
        else if (drawer.isDrawerOpen) drawer.closeDrawer()
        else super.onBackPressed()
    }

    override fun hasBottomBar():Boolean = false

}