package org.greeneyed.epl.librarian.services;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class VersionService {
    private static final String README_URL = "https://raw.githubusercontent.com/Verdoso/epl_librarian/master/README.md";

    private final PreferencesService preferencesService;

    @Getter
    private String latestVersion = null;

    private static final String RELEASE_INIT_TAG = "* Release ";

    private static final String RELEASE_END_TAG = ":";

    private static final int RELEASE_INIT_TAG_LENGTH = RELEASE_INIT_TAG.length();

    @PostConstruct
    public void postConstruct() {
        if(preferencesService.skipVersionCheck()) {
            log.warn("Comprobaci\u00f3n de versi\u00f3n deshabilitada, respetando decisi\u00f3n y no comprobando GitHub.");
        } else {
            this.findOutLatestVersion();
        }
    }

    private String findOutLatestVersion() {
        String ultimaVersion = null;
        HttpGet httpget = new HttpGet(README_URL);
        RequestConfig defaultRequestConfig = RequestConfig
                .custom()
                .setConnectionRequestTimeout((int) TimeUnit.MILLISECONDS.convert(50_000, TimeUnit.SECONDS))
                .setConnectTimeout((int) TimeUnit.MILLISECONDS.convert(50_000, TimeUnit.SECONDS))
                .setSocketTimeout((int) TimeUnit.MILLISECONDS.convert(50_000, TimeUnit.SECONDS))
                .build();
        try (CloseableHttpClient httpclient = HttpClients
                .custom()
                .setDefaultRequestConfig(defaultRequestConfig)
                .build();) {
            try (CloseableHttpResponse response = httpclient.execute(httpget)) {
                HttpEntity entity = response.getEntity();
                String readme = EntityUtils.toString(entity);
                int release = readme.indexOf(RELEASE_INIT_TAG);
                if (release > 0) {
                    int end = readme.indexOf(RELEASE_END_TAG, release);
                    if (end > 0) {
                        this.latestVersion = readme.substring(release + RELEASE_INIT_TAG_LENGTH, end);
                        log.info("Última versión de EPL Librarian publicada: {}", this.latestVersion);
                    }
                }
                EntityUtils.consume(entity);
            }
        } catch (IOException e) {
            log.error("Error obteniendo versión");
        }
        return ultimaVersion;
    }

}
