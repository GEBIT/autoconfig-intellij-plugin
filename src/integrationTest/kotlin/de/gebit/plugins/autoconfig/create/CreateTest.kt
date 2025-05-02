package de.gebit.plugins.autoconfig.create

import com.intellij.driver.sdk.invokeAction
import com.intellij.driver.sdk.ui.components.*
import com.intellij.driver.sdk.ui.present
import com.intellij.driver.sdk.ui.shouldBe
import com.intellij.driver.sdk.ui.xQuery
import com.intellij.driver.sdk.wait
import com.intellij.driver.sdk.waitForIndicators
import com.intellij.ide.IdeBundle
import com.intellij.ide.starter.driver.engine.runIdeWithDriver
import com.intellij.ide.starter.plugins.PluginConfigurator
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.util.SystemInfo
import de.gebit.plugins.autoconfig.builder.TestContextBuilder
import de.gebit.plugins.autoconfig.messages.AutoconfigBundle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.io.path.Path
import kotlin.time.Duration.Companion.minutes

/**
 * Simple round trip test, using settings dialog, Autoconfig file wizard, Run Autoconfig action and performing result checks.
 */
class CreateTest {

    private val popupList = listOf("Compiler settings compiler", "Debugger settings debugger")

    @ParameterizedTest
    @CsvSource(
        "IC, swesteme/artifact-version-service, main, 2024.3",
        "IC, swesteme/artifact-version-service, main, EAP"
    )
    fun compilerSharedHeapSizeSetting(
        ide: String,
        repo: String,
        branch: String,
        ideVersion: String
    ) {
        TestContextBuilder()
            .withIde(ide)
            .withRepository(repo)
            .withBranch(branch)
            .withIDEVersion(ideVersion)
            .build()
            .apply {
                val pathToPlugin = System.getProperty("path.to.build.plugin")
                PluginConfigurator(this).installPluginFromPath(Path(pathToPlugin))
            }
            .runIdeWithDriver().useDriverAndCloseIde {
                waitForIndicators(5.minutes)
                ideFrame {
                    assertFalse("64" == findConfiguredHeapSize())

                    waitForIndicators(1.minutes)

                    closeNotifications()

                    invokeAction("CreateAutoconfigFileAction", now = false)
                    dialog {
                        val comboBox =
                            comboBox(queryObjectByAccessibleNameKey("createautoconfigfile.selectfile", "JComboBox"))
                        val listValues = comboBox.listValues()
                        assertEquals(listValues.size, 5)
                        assertEquals(
                            listOf(
                                "Common configuration updater",
                                "Common module configuration updater (module)",
                                "Git configuration updater",
                                "Java configuration updater",
                                "Maven settings updater"
                            ), listValues
                        )
                        comboBox.selectItem("Java configuration updater")
                        pressButton("OK")
                    }

                    waitForNoOpenedDialogs()

                    // find content assist popup
                    val contentAssistPopup = popup().jBlist(xQuery { contains(byVisibleText(" Compiler settings")) })

                    // verify that the popup is open (and wait for it if necessary)
                    contentAssistPopup.shouldBe("Content assist list is not open", present)

                    val contentAssistItems = contentAssistPopup.items
                    assertEquals(popupList, contentAssistItems)

                    keyboard {
                        enter()
                    }

                    editor {
                        keyboard {
                            enterText("bu")
                            enter()
                            enterText("64")
                        }
                    }

                    invokeAction("RunAutoconfigAction", now = false)

                    val notification =
                        x(xQuery { byAccessibleName("New project configurations applied: Build process heap size") })
                    notification.shouldBe("Notification popup can not be found", present)

                    closeNotifications()

                    assertEquals("64", findConfiguredHeapSize())

//                    wait(1.minutes)
                }
            }
    }

    private fun IdeaFrameUI.closeNotifications() {
        xx(xQuery {
            byTooltip(
                IdeBundle.message(
                    "tooltip.close.notification",
                    if (SystemInfo.isMac) "⌥" else "Alt+"
                )
            )
        }).list()
            .forEach { t -> t.click() }
    }

    private fun IdeaFrameUI.findConfiguredHeapSize(): String =
        findConfiguredString("Shared heap size:", "Build, Execution, Deployment", "Compiler")

    private fun queryObjectByAccessibleName(accessibleName: String, objectClass: String): String = xQuery {
        and(
            byAccessibleName(accessibleName), byClass(objectClass)
        )
    }

    private fun queryObjectByAccessibleNameKey(accessibleNameKey: String, objectClass: String): String =
        queryObjectByAccessibleName(AutoconfigBundle.message(accessibleNameKey).replace("\u001B", ""), objectClass)

    private fun IdeaFrameUI.findConfiguredString(accessibleName: String, vararg clickPath: String): String {
        var configuredValue = ""
        openSettingsDialog()
        settingsDialog {
            tree("//div[@accessiblename='Settings categories']").clickPath(*clickPath)
            val textField = x(queryObjectByAccessibleName(accessibleName, "JBTextField"))
            textField.shouldBe("\"$accessibleName\" text field can not be found", present)
            configuredValue = textField.allTextAsString()
        }
        clickOkButton()
        return configuredValue
    }

    private fun IdeaFrameUI.clickOkButton() {
        dialog().pressButton("OK")
    }
}