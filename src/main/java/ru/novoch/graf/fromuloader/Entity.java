package ru.novoch.graf.fromuloader;

import java.time.LocalDate;
import java.util.Arrays;

public class Entity {
	private final static String vTempl_TypeUl = "Юридическое лицо";
	private final static String vTempl_TypeFl = "Физическое лицо";
	
	private Long ID;
	private String Name;
	private String NameEng;
	private String LastName;
	private String FirstName;
	private String MiddleName;
	private String FmtStr;
	private LocalDate BirthDate;
	private String BirthYear;
	private String DocName;
	private String DocSerNum;
	private String Primech;
	private String Uni;
	private LocalDate DateIncl;
	private int iType;
	private String cType;
	private String[] DifferNames;
	
	public Entity() {};
	
	public Long getID() {
		return ID;
	}
	public void setID(Long iD) {
		ID = iD;
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public String getLastName() {
		return LastName;
	}
	public void setLastName(String lastName) {
		LastName = lastName;
	}
	public String getFirstName() {
		return FirstName;
	}
	public void setFirstName(String firstName) {
		FirstName = firstName;
	}
	public String getMiddleName() {
		return MiddleName;
	}
	public void setMiddleName(String middleName) {
		MiddleName = middleName;
	}
	public String getFmtStr() {
		return FmtStr;
	}
	public void setFmtStr(String fmtStr) {
		FmtStr = fmtStr;
	}
	public LocalDate getBirthDate() {
		return BirthDate;
	}
	public void setBirthDate(LocalDate birthDate) {
		BirthDate = birthDate;
	}
	public String getBirthYear() {
		return BirthYear;
	}
	public void setBirthYear(String birthYear) {
		BirthYear = birthYear;
	}
	public String getDocName() {
		return DocName;
	}
	public void setDocName(String docName) {
		DocName = docName;
	}
	public String getDocSerNum() {
		return DocSerNum;
	}
	public void setDocSerNum(String docSerNum) {
		DocSerNum = docSerNum;
	}
	public String getPrimech() {
		return Primech;
	}
	public void setPrimech(String primech) {
		Primech = primech;
	}
	public String getUni() {
		return Uni;
	}
	public void setUni(String uni) {
		Uni = uni;
	}
	public LocalDate getDateIncl() {
		return DateIncl;
	}
	public void setDateIncl(LocalDate dateIncl) {
		DateIncl = dateIncl;
	}

	public int getiType() {
		return iType;
	}

	public void setiType(int iType) {
		this.iType = iType;
		switch (iType) {
		case 1: setcType(vTempl_TypeUl); break;
		case 2: setcType(vTempl_TypeFl); break;
		}
	}

	public String getcType() {
		return cType;
	}

	public void setcType(String cType) {
		this.cType = cType;
	}

	public String[] getDifferNames() {
		return DifferNames;
	}

	public void setDifferNames(String[] differNames) {
		DifferNames = differNames;
	}

	public String getNameEng() {
		return NameEng;
	}

	public void setNameEng(String nameEng) {
		NameEng = nameEng;
	}
	@Override
	public String toString() {
		return "Entity [ID=" + ID + ", Name=" + Name + ", NameEng=" + NameEng + ", LastName=" + LastName
				+ ", FirstName=" + FirstName + ", MiddleName=" + MiddleName
				+ ", FmtStr=" + FmtStr + ", BirthDate=" + BirthDate + ", BirthYear=" + BirthYear + ", DocName="
				+ DocName + ", DocSerNum=" + DocSerNum + ", Primech=" + Primech + ", Uni=" + Uni + ", DateIncl="
				+ DateIncl + ", iType=" + iType + ", cType=" + cType + ", DifferNames=" + Arrays.toString(DifferNames)
				+ "]";
	}
}
