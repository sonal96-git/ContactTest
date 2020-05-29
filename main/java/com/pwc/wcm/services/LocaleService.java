package com.pwc.wcm.services;

/**
 * Provides Information related to locale
 */
public interface LocaleService {
	/**
	 * method returns language default value for the language code provided (i.e. en-al returns 'English' in albanian )
	 * @param languageCode {@link String}
	 * @return LanguageDefault value for language code
	 */
	String getLanguageOriginalFromLanguageCode(String languageCode);

	/**
	 * method returns language default value for the locale provided (i.e. en_al returns 'English' in albanian )
	 * @param locale {@link String}
	 * @return LanguageDefault value for locale
	 */
	String getLanguageOriginalFromLocale(String locale);
}
