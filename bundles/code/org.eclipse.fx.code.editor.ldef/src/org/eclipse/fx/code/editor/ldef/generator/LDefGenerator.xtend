/*
 * generated by Xtext
 */
package org.eclipse.fx.code.editor.ldef.generator

import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.IGenerator
import org.eclipse.xtext.generator.IFileSystemAccess
import org.eclipse.fx.code.editor.ldef.lDef.Root
import com.google.inject.Inject
import org.eclipse.fx.code.editor.ldef.lDef.JavaFXIntegration

/**
 * Generates code from your model files on save.
 *
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#code-generation
 */
class LDefGenerator implements IGenerator {
	@Inject
	JavaFXCodeGenerator generator;
	@Inject
	JSONConfigurationConfigurator jsonGenerator;

	override void doGenerate(Resource resource, IFileSystemAccess fsa) {
		val root = resource.contents.head as Root
		jsonGenerator.generate(root.languageDefinition,fsa)
		if( root.languageDefinition.integration != null ) {
			if( ! root.languageDefinition.integration.codeIntegrationList.filter(typeof(JavaFXIntegration)).empty ) {
				generator.generate(root.languageDefinition, fsa);
			}
		}
	}
}
