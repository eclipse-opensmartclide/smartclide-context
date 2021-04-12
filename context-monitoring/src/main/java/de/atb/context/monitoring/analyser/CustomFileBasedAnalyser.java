package de.atb.context.monitoring.analyser;

import de.atb.context.monitoring.analyser.file.FileAnalyser;
import de.atb.context.monitoring.config.models.DataSource;
import de.atb.context.monitoring.config.models.InterpreterConfiguration;
import de.atb.context.monitoring.config.models.datasources.FileSystemDataSource;
import de.atb.context.monitoring.index.Indexer;
import de.atb.context.monitoring.models.CustomFileBasedDataModel;
import de.atb.context.monitoring.parser.IndexedFields;
import de.atb.context.tools.ontology.AmIMonitoringConfiguration;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

public class CustomFileBasedAnalyser extends FileAnalyser<CustomFileBasedDataModel> {

    private final Logger logger = LoggerFactory.getLogger(CustomFileBasedAnalyser.class);

    public CustomFileBasedAnalyser(DataSource dataSource, InterpreterConfiguration interpreterConfiguration, Indexer indexer, Document document, AmIMonitoringConfiguration amiConfiguration) {
        super(dataSource, interpreterConfiguration, indexer, document, amiConfiguration);
    }

    @Override
    public List<CustomFileBasedDataModel> analyseObject(File file) {
        logger.debug("Analysing '" + file.getAbsolutePath() + "'");

        List<CustomFileBasedDataModel> customFileBasedDataModels = new LinkedList<>();
        JSONParser parser = new JSONParser();

        try {
            // read json file and parse data to JSONArray
            JSONArray jsonArray = (JSONArray) parser.parse(new FileReader(
                file));

            // for each
            for (Object object : jsonArray) {
                JSONObject jsonObject = (JSONObject) object;

                CustomFileBasedDataModel customFileBasedDataModel = new CustomFileBasedDataModel();

                // read data from JSON file and save it to CustomFileBasedDataModel
                // TODO: add more fields to json file?
                logger.debug("Json object value" + jsonObject);
                customFileBasedDataModel.setMessage((String)jsonObject.get("message"));
                customFileBasedDataModel.setUserInfo((String)jsonObject.get("user-info"));
                customFileBasedDataModel.setSource((String)jsonObject.get("source"));

                // Add additional fields
                customFileBasedDataModel.setDocumentUri(IndexedFields.Uri.getString(this.document));
                customFileBasedDataModel.setDocumentIndexId(IndexedFields.IndexId
                    .getString(this.document));
                try {
                    customFileBasedDataModel.setMonitoredAt(DateTools.stringToDate(IndexedFields.MonitoredAt.getString(this.document)));
                } catch (ParseException e) {
                    logger.error(e.getMessage(), e);
                }
                customFileBasedDataModel.setDataSource(this.dataSource
                    .convertTo(FileSystemDataSource.class));

                // add customFileBasedDataModel to return list
                customFileBasedDataModels.add(customFileBasedDataModel);

            } // end of for loop
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }
        return customFileBasedDataModels;
    }
}
