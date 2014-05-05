package ru.yandex.summer.reflectionjdbc.api.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for loading queries from XMLHelper and accessing them by query id
 * 
 * @author Arsen Alexanyan
 */
public class SQLQueries
		implements Queries {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private String filePath = null;

	private DBType dbType = null;

	private Map<String, String> queries = new HashMap<String, String>();

	/**
	 * Constructs new object of this class. Package protected constructor available for {@link QueriesProvider}
	 * 
	 * @param dbType
	 *            Provided database type. See {@link DBType}
	 * @param filePath
	 *            Provided xml file path, where are stored the queries.

	 */
	SQLQueries(DBType dbType, String filePath) {
		this.filePath = filePath;
		this.dbType = dbType;
		loadXml();
		logger.info("The file \""
				+ filePath
				+ "\" has succesfully parsed for \""
				+ dbType.getTitle()
				+ "\" database type");
	}

	/**
	 * Loads queries from XMLHelper with specified database type and xml file path
	 */
	private void loadXml()  {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setValidating(true);
			factory.setNamespaceAware(true);

			SAXParser parser = factory.newSAXParser();

			DefaultHandler handler = new DefaultHandler() {
				private boolean acceptName = false;
				private String queryName = null;
				private boolean isInDialect = false;

				public void startElement(String namespaceURI, String lname, String qname, Attributes attrs)
						throws SAXException {
					if (isInDialect) {
						if (qname.equalsIgnoreCase(XMLHelper.LANGUAGE) && attrs != null) {
							for (int i = 0; i < attrs.getLength(); i++) {
								String aname = attrs.getLocalName(i);
								if (aname.equalsIgnoreCase(XMLHelper.NAME_ATTR)) {
									if (attrs.getValue(i).equalsIgnoreCase(dbType.getTitle())) {
										acceptName = true;
									}
									break;
								}
							}
						}
						return;
					}

					if (queryName != null) {
						if (qname.equalsIgnoreCase(XMLHelper.DIALECTS)) {
							isInDialect = true;
						}
					}

					if (qname.equalsIgnoreCase(XMLHelper.QUERY) && attrs != null) {
						for (int i = 0; i < attrs.getLength(); i++) {
							String aname = attrs.getLocalName(i);
							if (aname.equalsIgnoreCase(XMLHelper.ID_ATTR)) {
								queryName = attrs.getValue(i);
								break;
							}
						}
					}
				}

				@Override
				public void characters(char[] ch, int start, int length)
						throws SAXException {
					if (isInDialect) {
						String str = new String(ch, start, length).trim();
						if (acceptName && str != null && !str.isEmpty()) {
							if (queryName != null) {
								queries.put(queryName, str);
							}
							acceptName = false;
						}
					}
				}

				@Override
				public void endElement(String uri, String lname, String qname)
						throws SAXException {
					if (qname.equalsIgnoreCase(XMLHelper.QUERY)) {
						queryName = null;
					}
					if (qname.equalsIgnoreCase(XMLHelper.DIALECTS)) {
						isInDialect = false;
					}
				}
			};

			parser.parse(getClass().getResourceAsStream(filePath), handler);
		}
		catch (ParserConfigurationException | SAXException |IOException e) {
			logger.error("Error in parsing xml file.", e);
			throw new QueriesParseException(e.getMessage(), e);
		}
	}

	/**
	 * Gets query by provided query id
	 * 
	 * @param id
	 *            Provided query id
	 * @return Sql Query
	 */
	public String getQuery(String id) {
		return queries.get(id);
	}

	/**
	 * Class for XMLHelper tag and attribute constants for loading queries in {@link SQLQueries}
	 *
	 *
	 */
	class XMLHelper {

		public static final String QUERY = "query";
		public static final String ID_ATTR = "id";
		public static final String DIALECTS = "dialects";
		public static final String LANGUAGE = "language";
		public static final String NAME_ATTR = "name";
		//public static final String METADATA = "metadata";
	}

}
