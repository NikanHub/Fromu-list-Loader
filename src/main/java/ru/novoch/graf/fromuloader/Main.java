package ru.novoch.graf.fromuloader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static java.util.Locale.*;
import org.jsoup.Jsoup;  
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * 
 * @author Nikan
 * v 1.0
 * Программа позволяет получить данные о ФРОМУ с указанного URL в формате таблицы с определенными полями
 * и сформировать xml-файл нужного формата для загрузки перечня в ЦАБС.<br>
 * Чувствительна к формату данных, при любых изменениях требует доработки.<br>
 */

public class Main {
	// для файла настроек
	private final static String cHttp = "HTTP";
	private final static String cFileDir = "FILE_DIR";
	private final static String cLogDir = "LOG_DIR";
	private final static String cSepar = "=";
	private final static String cRem = "#";
	// регулярки для получения данных
	private final static String vTempl_BirthDate = "Дата рождения";
	private final static String vTempl_BirthPlace = "Место рождения";
	private final static String vTempl_BR_Fl = "Обращение";
	private final static String vTempl_BR_Ul = "Вымышленные названия";
	private final static String vTempl_Primech = "Прочая информация";
	
	private static String vHttp = "";
	private static String vFileDir = "";
	private static String vLogDir = "";
	
	static org.jsoup.nodes.Document docHtml;
	static org.w3c.dom.Document docXml;
    
	private static boolean isNotNullOrEmpty(String s) {
        return s != null && !s.isEmpty();
    }
    
	private static String checkSlash(String str) {
		String strRez = str;
		if (!"\\".equals(strRez.substring(strRez.length()-1)))
			strRez = strRez.concat("\\\\");
		return strRez;
	}
	
	private static String formatStr(String str) {
		String rezStr = "#".concat(str).concat("#");
		String[] badKeys = new String[] {"ЛТД","КО","CO","ИНТЕРНЭШНЛ","АЛЬ","И","А К А","ИЛИ","A K A","F K A","OF","FOR","AND","LTD","INC","BANK","COMPANY","PRIVATE","LIMITED","INTERNATIONAL","GROUP","IN","AS","THE","TO"};
		int i = 0;
		for (i=0;i<badKeys.length;i++) {
			rezStr = rezStr.replace(" ".concat(badKeys[i]).concat(" "), " ");
			rezStr = rezStr.replace(",".concat(badKeys[i]).concat(" "), "#");
			rezStr = rezStr.replace(" ".concat(badKeys[i]).concat(","), "#");
			rezStr = rezStr.replace(",".concat(badKeys[i]).concat(","), "#");
			rezStr = rezStr.replace(" ".concat(badKeys[i]).concat("#"), "#");
			rezStr = rezStr.replace("#".concat(badKeys[i]).concat(" "), "#");
		}
		return rezStr.replace("#","").trim();
	}
    
    static Logger LOGGER;

	public static void main(String[] args) {
		// чтение настроек из файла %XXI_HOME%/BIN/fromuloader.properties
		String cXXI_HOME = checkSlash(System.getenv("XXI_HOME"));
		try {
			FileReader f_opt = new FileReader(cXXI_HOME.concat("BIN\\fromuloader.properties"));
			Scanner f_scan = new Scanner(f_opt);
			while (f_scan.hasNextLine()) {
				String str = f_scan.nextLine();
				if (!str.substring(0,1).equals(cRem)) {
					if (str.substring(0,str.indexOf(cSepar)).equals(cHttp) ){
						vHttp = str.substring(str.indexOf(cSepar)+cSepar.length());
					}
					else if (str.substring(0,str.indexOf(cSepar)).equals(cFileDir) ){
						vFileDir = checkSlash(str.substring(str.indexOf(cSepar)+cSepar.length()));
					}
					else if (str.substring(0,str.indexOf(cSepar)).equals(cLogDir) ){
						vLogDir = checkSlash(str.substring(str.indexOf(cSepar)+cSepar.length()));
					}
				}
			}
			f_scan.close();
			f_opt.close();
		} catch (IOException e) {
			System.out.println("Error in read file fromuloader.properties: ".concat(e.toString()));
			System.exit(0);
		}
		
		System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %4$s %2$s %5$s%6$s%n");
		LOGGER = Logger.getLogger("ExtrLog"); 
		FileHandler fh;
		try {
	        String cLogFile = vLogDir.concat("fromuloader.log");
			fh = new FileHandler(cLogFile);
			LOGGER.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter(); 
	        fh.setFormatter(formatter);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		LOGGER.log(Level.INFO, "Fromu loader begin");
		
		docHtml = null;
		
		List<Entity> arrElements = new ArrayList<Entity>();
		
		try {
			docHtml = Jsoup.connect(vHttp).get();
		} catch (IOException e) {
			//System.out.println("Error load http: ".concat(vHttp));
			LOGGER.log(Level.SEVERE, "Error load http: ".concat(vHttp).concat(e.getMessage()));
			System.exit(0);
		}  
		
		Elements aElements = docHtml.getElementsByTag("tr");
		LOGGER.log(Level.INFO, "fromu count = ".concat(String.valueOf(aElements.size())));
		for (Element aElement : aElements) {
			Entity ent = new Entity();
			Elements aElements1 = aElement.getElementsByTag("td");
			int ii = 0;
			String str = null; 
			for (Element aElement1 : aElements1) {
				ii += 1; 
				try {
					switch (ii) {
					case 1: ent.setID(Long.valueOf(aElement1.text())); break;
					case 2:
						str = aElement1.text();
						/*str = "7TH OF TIR, КОМПАНИЯ «7-ГО ТИРА»\r\n"
								+ "Вымышленные названия: д/о Р.И.К.: д/о Адрес: д/о Дата внесения в перечень: 23 Dec. 2006 ( изменения внесены 17 Dec. 2014 ) Прочая информация: подведомственна Организации оборонной промышленности (ООП), которая, как широко признается, принимает непосредственное участие в осуществлении ядерной программы. [Прежний идентификационный номер — E.37.A.7]" ;
						*/
						// парсим сведения и субъекте
						if (isNotNullOrEmpty(str)) {
							// тип клиента
							if (str.indexOf(vTempl_BirthDate) == -1) {
								// ЮЛ
								ent.setiType(1);
								String name = str.substring(0, str.indexOf(vTempl_BR_Ul)).replace("«", "").replace("»", "").trim();
								String formatName = formatStr(name);
								if (formatName.indexOf(",") != -1) {
									String differName = formatName.substring(formatName.indexOf(",")+1).trim();
									String[] differNames = differName.split(",");
									for(int i = 0; i < differNames.length; i++) {
										differNames[i] = differNames[i].trim();
								      }
									ent.setDifferNames(differNames);
									if (isNotNullOrEmpty(differNames[differNames.length-1])) {
										ent.setName(differNames[differNames.length-1]);
										ent.setNameEng(name.substring(0,name.indexOf(",")).replaceAll(",", "").trim());
									}
									else {
										ent.setName(name.substring(0,name.indexOf(",")).replaceAll(",", "").trim());
										ent.setNameEng(ent.getName());
									}
								}
								else {
									ent.setName(name);
									ent.setNameEng(ent.getName());
								}
								ent.setFmtStr(ent.getName());
								//System.out.println(ent.toString());
							}
							else {
								// ФЛ
								ent.setiType(2);
								String name = str.substring(0, str.indexOf(vTempl_BR_Fl)).replace("«", "").replace("»", "").trim();
								String formatName = formatStr(name);
								if (formatName.indexOf(",") != -1) {
									String differName = formatName.substring(formatName.indexOf(",")+1).trim();
									String[] differNames = differName.split(",");
									for(int i = 0; i < differNames.length; i++) {
										differNames[i] = differNames[i].trim();
								      }
									ent.setDifferNames(differNames);
									if (isNotNullOrEmpty(differNames[differNames.length-1])) {
										ent.setName(differNames[differNames.length-1]);
										ent.setNameEng(name.substring(0,name.indexOf(",")).replaceAll(",", "").trim());
									}
									else {
										ent.setName(name.substring(0,name.indexOf(",")).replaceAll(",", "").trim());
										ent.setNameEng(ent.getName());
									}
								}
								else {
									ent.setName(name);
									ent.setNameEng(ent.getName());
								}
								ent.setLastName(ent.getName());
								ent.setFmtStr(ent.getName());
								String sBirthDate = str.substring(str.indexOf(vTempl_BirthDate)+vTempl_BirthDate.length()+2, str.indexOf(vTempl_BirthPlace)-1);
								ent.setBirthDate(LocalDate.parse(sBirthDate,DateTimeFormatter.ofPattern("dd MMM. yyyy",ENGLISH)));
								ent.setBirthYear(ent.getBirthDate().format(DateTimeFormatter.ofPattern("yyyy")));
							}
							// Примечание
							ent.setPrimech(str.substring(str.indexOf(vTempl_Primech)+vTempl_Primech.length()+2));
						}
						break;
					case 4: ent.setUni(aElement1.text()); break;
					case 5: ent.setDateIncl(LocalDate.parse(aElement1.text(),DateTimeFormatter.ofPattern("dd.MM.yyyy"))); break;
					}
				} catch (Exception e) {
					LOGGER.log(Level.WARNING, e.toString().concat("\n").concat(aElement1.text()).concat("\n"));
					continue;
				}
			}
			if (isNotNullOrEmpty(ent.getUni())
					&& ent.getDateIncl() != null
					&& ent.getID() != null
					&& isNotNullOrEmpty(ent.getName())
					)
				arrElements.add(ent);
		}
		
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	        docXml = docBuilder.newDocument();
	        //docXml.setXmlStandalone(true);
	        org.w3c.dom.Element SpisokOMU = docXml.createElement("СписокОМУ");
	        SpisokOMU.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xs", "http://www.w3.org/2001/XMLSchema-instance");
	        docXml.appendChild(SpisokOMU);
	        org.w3c.dom.Element VersionFormat = docXml.createElement("ВерсияФормата");
	        VersionFormat.setTextContent("1.0");
	        SpisokOMU.appendChild(VersionFormat);
	        org.w3c.dom.Element DateSpisok = docXml.createElement("ДатаСписка");
	        DateSpisok.setTextContent(LocalDate.now().toString());
	        SpisokOMU.appendChild(DateSpisok);
	        org.w3c.dom.Element DateLastSpisok = docXml.createElement("ДатаПредыдущегоСписка");
	        DateLastSpisok.setTextContent(LocalDate.now().minusDays(1L).toString());
	        SpisokOMU.appendChild(DateLastSpisok);
	        org.w3c.dom.Element ActualSpisok = docXml.createElement("АктуальныйСписок");
	        SpisokOMU.appendChild(ActualSpisok);
			for (Entity aEnt : arrElements) {
				//System.out.println(aEnt.toString());
				org.w3c.dom.Element Subject = docXml.createElement("Субъект");
				ActualSpisok.appendChild(Subject);
				org.w3c.dom.Element SubjectId = docXml.createElement("ИдСубъекта");
				SubjectId.setTextContent(aEnt.getID().toString());
				Subject.appendChild(SubjectId);
				org.w3c.dom.Element SubjectUNI = docXml.createElement("УНС");
				SubjectUNI.setTextContent(aEnt.getUni());
				Subject.appendChild(SubjectUNI);
				org.w3c.dom.Element SubjectType = docXml.createElement("ТипСубъекта");
				org.w3c.dom.Element SubjectIType = docXml.createElement("Идентификатор");
				SubjectIType.setTextContent(String.valueOf(aEnt.getiType()));
				SubjectType.appendChild(SubjectIType);
				org.w3c.dom.Element SubjectCType = docXml.createElement("Наименование");
				SubjectCType.setTextContent(String.valueOf(aEnt.getcType()));
				SubjectType.appendChild(SubjectCType);
				Subject.appendChild(SubjectType);
				org.w3c.dom.Element SubjectHistory = docXml.createElement("История");
				Subject.appendChild(SubjectHistory);
				org.w3c.dom.Element SubjectDateIncl = docXml.createElement("ДатаВключения");
				SubjectDateIncl.setTextContent(String.valueOf(aEnt.getDateIncl()));
				SubjectHistory.appendChild(SubjectDateIncl);
				switch (aEnt.getiType()) {
				case 1:
					org.w3c.dom.Element SubjectUL = docXml.createElement("ЮЛ");
					org.w3c.dom.Element SubjectULName = docXml.createElement("Наименование");
					SubjectULName.setTextContent(aEnt.getName());
					SubjectUL.appendChild(SubjectULName);
					if (isNotNullOrEmpty(aEnt.getNameEng())) {
						org.w3c.dom.Element SubjectULNameEng = docXml.createElement("НаименованиеЛат");
						SubjectULNameEng.setTextContent(aEnt.getNameEng());
						SubjectUL.appendChild(SubjectULNameEng);
					}
					//почему-то не загружает с этим
					if (aEnt.getDifferNames() != null && aEnt.getDifferNames().length > 1) {
						org.w3c.dom.Element SubjectULDifferNames = docXml.createElement("СписокДрНаименований");
						for (String differName : aEnt.getDifferNames()) {
							org.w3c.dom.Element SubjectULDifferName = docXml.createElement("ДрНаименование");
							org.w3c.dom.Element SubjectULDifferNameName = docXml.createElement("Наименование");
							SubjectULDifferNameName.setTextContent(differName);
							SubjectULDifferName.appendChild(SubjectULDifferNameName);
							SubjectULDifferNames.appendChild(SubjectULDifferName);
						}
						SubjectUL.appendChild(SubjectULDifferNames);
					}
					Subject.appendChild(SubjectUL);
					break;
				case 2:
					org.w3c.dom.Element SubjectFL = docXml.createElement("ФЛ");
					org.w3c.dom.Element SubjectFIO = docXml.createElement("ФИО");
					SubjectFIO.setTextContent(aEnt.getName());
					SubjectFL.appendChild(SubjectFIO);
					org.w3c.dom.Element SubjectLastName = docXml.createElement("Фамилия");
					SubjectLastName.setTextContent(aEnt.getName());
					SubjectFL.appendChild(SubjectLastName);
					if (isNotNullOrEmpty(aEnt.getNameEng())) {
						org.w3c.dom.Element SubjectLastNameEng = docXml.createElement("ФИОЛат");
						SubjectLastNameEng.setTextContent(aEnt.getNameEng());
						SubjectFL.appendChild(SubjectLastNameEng);
					}
					if (aEnt.getDifferNames() != null && aEnt.getDifferNames().length > 1) {
						org.w3c.dom.Element SubjectFLDifferNames = docXml.createElement("СписокДрНаименований");
						for (String differName : aEnt.getDifferNames()) {
							org.w3c.dom.Element SubjectFLDifferName = docXml.createElement("ДрНаименование");
							org.w3c.dom.Element SubjectULDifferNameFIO = docXml.createElement("ФИО");
							SubjectULDifferNameFIO.setTextContent(differName);
							SubjectFLDifferName.appendChild(SubjectULDifferNameFIO);
							SubjectFLDifferNames.appendChild(SubjectFLDifferName);
						}
						SubjectFL.appendChild(SubjectFLDifferNames);
					}
					if (aEnt.getBirthDate() != null) {
						org.w3c.dom.Element SubjectBirthDate = docXml.createElement("ДатаРождения");
						SubjectBirthDate.setTextContent(aEnt.getBirthDate().toString());
						SubjectFL.appendChild(SubjectBirthDate);
					}
					org.w3c.dom.Element SubjectBirthYear = docXml.createElement("ГодРождения");
					SubjectBirthYear.setTextContent(aEnt.getBirthYear());
					SubjectFL.appendChild(SubjectBirthYear);
					Subject.appendChild(SubjectFL);
					break;
				}
				org.w3c.dom.Element SubjectPrimech = docXml.createElement("Примечание");
				SubjectPrimech.setTextContent(aEnt.getPrimech());
				Subject.appendChild(SubjectPrimech);
			}
        } catch (Exception e) {
     	   //System.out.println("Error in create xml: ".concat(e.toString()));
        	LOGGER.log(Level.SEVERE, "Error in create xml: ".concat(e.toString()).concat(" / "));
     	   e.getStackTrace();
     	   System.exit(0);
        }
		
		try {
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        Transformer transformer = transformerFactory.newTransformer();
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        DOMSource source = new DOMSource(docXml);
	        String cFile = vFileDir.concat("fromu_".concat(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")).toString()).concat(".xml"));
	        StreamResult result = new StreamResult(new File(cFile));
	        transformer.transform(source, result);
	        //System.out.println("File saved: ".concat(cFile.replace("\\\\", "\\")));
	        LOGGER.log(Level.INFO, "File saved: ".concat(cFile.replace("\\\\", "\\")));
        } catch (Exception e) {
     	   //System.out.println("Error in save file: ".concat(e.toString()));
        	LOGGER.log(Level.SEVERE, "Error in save file: ".concat(e.toString()));
     	   e.getStackTrace();
        }
		
	}

}
