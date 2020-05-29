const gwtStartupScript = document.currentScript.getAttribute('data-gwt-startup-src');

/*const readFavoriteLanguageCallback = function (result) {
    const userFavoriteLanguage = (result && result.favoriteLanguage) || 'en';

    chrome.storage.local.set({ favoriteLanguage: userFavoriteLanguage }, () => {
        // create the GWT meta tag that allows the GWT bootstrap to choose the right permutation
        const gwtLocaleMetaTag = document.createElement('meta');
        gwtLocaleMetaTag.setAttribute('name', 'gwt:property');
        gwtLocaleMetaTag.content = `locale=${userFavoriteLanguage}`;
        document.head.appendChild(gwtLocaleMetaTag);

        // then inject the GWT bootstrap code
        const gwtBootstrapScriptTag = document.createElement('script');
        gwtBootstrapScriptTag.setAttribute('src', gwtStartupScript);

        document.body.appendChild(gwtBootstrapScriptTag);
    });
};

chrome.storage.local.get('favoriteLanguage', readFavoriteLanguageCallback);*/
