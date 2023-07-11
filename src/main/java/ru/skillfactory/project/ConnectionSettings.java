package ru.skillfactory.project;

import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.io.File;


@Data
@XmlRootElement(name = "Settings")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"ip", "port", "databaseName", "login", "password"})
public class ConnectionSettings {

    private static Logger log = LogManager.getLogger(ConnectionSettings.class);

    private String ip;
    private String port;
    private String login;
    private String password;
    private String databaseName;

    public String getUrl() {
        return "jdbc:postgresql://" + ip + ":" + port + "/";
    }

    public static void saveSettings(String fileName, ConnectionSettings settings) {
        try {
            JAXBContext context = JAXBContext.newInstance(ConnectionSettings.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(settings, new File(fileName));
        } catch (JAXBException e) {
            log.warn("не удалось сохранить настройки соединения с БД");
            log.warn(e.getMessage());
        }
    }

    public static ConnectionSettings loadSettings (String fileName) {
        ConnectionSettings settings = new ConnectionSettings();
        try {
            JAXBContext context = JAXBContext.newInstance(ConnectionSettings.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            settings = (ConnectionSettings) unmarshaller.unmarshal(new File(fileName));
        } catch (JAXBException e) {
            log.warn("не удалось загрузить настройки соединения с БД");
            log.warn(e.getMessage());
        }
        return settings;
    }

}
