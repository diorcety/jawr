/**
 * Copyright 2009 Ibrahim Chaehoi
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package net.jawr.web.resource.bundle;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import net.jawr.web.resource.bundle.factory.PropertiesBundleConstant;
import net.jawr.web.resource.bundle.postprocess.ChainedResourceBundlePostProcessor;
import net.jawr.web.util.StringUtils;

/**
 * This class will manage the serialization of the joinable resource bundle.
 * 
 * @author Ibrahim Chaehoi
 * 
 */
public class JoinableResourceBundlePropertySerializer {

	/**
	 * This method will serialize the properties of the bundle in the Properties object
	 * 
	 * This method will serialize all bundle except the childs of composite bundle,
	 * for which the mappings will be part of their parent bundles.  
	 * The properties associated to a bundle are the same as the one define in the jawr configuration file.
	 * Only the following properties are different from the standard configuration file :
	 *   - The mapping, which will contains path to each resources of the bundle ( no wildcard like : myfolder/** ) 
	 *   - The locale variants which will be explicitly specified.
	 *   - The bundle hash codes will be define as properties, so we will not have to compute them.
	 *   For a bundle with local variants, there will be an hash code for each variant + one, which is the hash 
	 *   code of the default bundle.
	 *   - The licence path list.
	 *   
	 * @param bundle the bundle to serialize
	 * @param props the properties to update
	 */
	public static void serializeInProperties(JoinableResourceBundle bundle,
			String type, Properties props) {

		// If the bundle is a child of a composite bundle, 
		// no need to serialize it, it will be integrated with the composite bundle
		if (StringUtils.isEmpty(bundle.getId())) {
			return;
		}

		String bundleName = bundle.getName();
		String prefix = PropertiesBundleConstant.PROPS_PREFIX + type + "."+ PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_PROPERTY + bundleName;
		InclusionPattern inclusion = bundle.getInclusionPattern();

		// Set the ID
		props.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ID, bundle.getId());

		if (inclusion.isGlobal()) {
			props
					.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_GLOBAL_FLAG, Boolean.toString(inclusion
							.isGlobal()));
		}
		if (inclusion.getInclusionOrder() != 0) {
			props.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ORDER, Integer.toString(inclusion
					.getInclusionOrder()));
		}

		if (inclusion.isIncludeOnDebug()) {
			props.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGONLY, Boolean.toString(inclusion
					.isIncludeOnDebug()));
		}
		if (inclusion.isExcludeOnDebug()) {
			props.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGNEVER, Boolean.toString(inclusion
					.isExcludeOnDebug()));
		}
		if (StringUtils.isNotEmpty(bundle.getExplorerConditionalExpression())) {
			props.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_IE_CONDITIONAL_EXPRESSION, bundle
					.getExplorerConditionalExpression());
		}
		if (StringUtils.isNotEmpty(bundle.getAlternateProductionURL())) {
			props.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_PRODUCTION_ALT_URL, bundle
					.getAlternateProductionURL());
		}

		if (bundle.getBundlePostProcessor() != null) {
			props
					.put(prefix +PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_POSTPROCESSOR,
							getBundlePostProcessorsName((ChainedResourceBundlePostProcessor) bundle
									.getBundlePostProcessor()));
		}

		if (bundle.getUnitaryPostProcessor() != null) {
			props.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_FILE_POSTPROCESSOR,
					getBundlePostProcessorsName((ChainedResourceBundlePostProcessor) bundle
							.getUnitaryPostProcessor()));
		}
		// Add locales and the bundle hashcode
		List localeVariantKeys = bundle.getLocaleVariantKeys();
		if (localeVariantKeys != null && !localeVariantKeys.isEmpty()) {
			String locales = getCommaSeparatedString(localeVariantKeys);
			if (StringUtils.isNotEmpty(locales)) {
				props.put(prefix +  PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_LOCALE_VARIANTS, locales);
			}

			for (Iterator iterator = localeVariantKeys.iterator(); iterator
					.hasNext();) {
				String localeVariantKey = (String) iterator.next();
				if (StringUtils.isNotEmpty(localeVariantKey)) {
					props.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE_VARIANT + localeVariantKey,
							bundle.getBundleDataHashCode(localeVariantKey));
				}
			}
		} 
			
		props.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE, bundle
					.getBundleDataHashCode(null));
		

		// mapping
		List itemPathList = bundle.getItemPathList();
		if (itemPathList != null && !itemPathList.isEmpty()) {
			props
					.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_MAPPINGS,
							getCommaSeparatedString(itemPathList));
		}

		Set licensesPathList = bundle.getLicensesPathList();
		if (licensesPathList != null && !licensesPathList.isEmpty()) {
			props.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_LICENCE_PATH_LIST,
					getCommaSeparatedString(licensesPathList));
		}
	}

	/**
	 * Returns the mapping list
	 * 
	 * @param itemPathList the item path list
	 * @return the item path list
	 */
	private static String getCommaSeparatedString(Collection coll) {

		StringBuffer buffer = new StringBuffer();
		for (Iterator eltIterator = coll.iterator(); eltIterator.hasNext();) {
			String elt = (String) eltIterator.next();
			if (StringUtils.isNotEmpty(elt)) {
				buffer.append(elt);
				if (eltIterator.hasNext()) {
					buffer.append(",");
				}
			}
		}
		return buffer.toString();
	}

	/**
	 * Returns the bundle post processor name separated by a comma character
	 * 
	 * @param processor the post processor
	 * @return the bundle post processor name separated by a comma character
	 */
	private static String getBundlePostProcessorsName(
			ChainedResourceBundlePostProcessor processor) {

		String bundlePostProcessor = "";
		if (processor != null) {
			if (processor != null) {
				bundlePostProcessor = processor.getId();
			}
		}

		return bundlePostProcessor;
	}
}