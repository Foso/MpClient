package de.jensklingenberg.cabret

import com.google.auto.service.AutoService
import com.intellij.mock.MockProject

import de.jensklingenberg.cabret.compiler.CabretIrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.config.kotlinSourceRoots
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.extensions.DeclarationAttributeAltererExtension

@AutoService(ComponentRegistrar::class)
class NativeComponentRegistrar : ComponentRegistrar {


    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {

        if (configuration[KEY_ENABLED] == false) {
            return
        }

        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)


        //Debuglog
        IrGenerationExtension.registerExtension(project, CabretIrGenerationExtension(messageCollector))
        DeclarationAttributeAltererExtension.registerExtension(project,object : DeclarationAttributeAltererExtension {

        })
    }
}
