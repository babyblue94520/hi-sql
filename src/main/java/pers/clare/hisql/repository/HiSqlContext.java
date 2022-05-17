package pers.clare.hisql.repository;

import pers.clare.hisql.naming.NamingStrategy;
import pers.clare.hisql.page.PaginationMode;

@SuppressWarnings("unused")
public class HiSqlContext {

    private String xmlRoot;

    private PaginationMode paginationMode;

    private NamingStrategy naming;

    public String getXmlRoot() {
        return xmlRoot;
    }

    public void setXmlRoot(String xmlRoot) {
        this.xmlRoot = xmlRoot;
    }

    public PaginationMode getPaginationMode() {
        return paginationMode;
    }

    public void setPaginationMode(PaginationMode paginationMode) {
        this.paginationMode = paginationMode;
    }

    public NamingStrategy getNaming() {
        return naming;
    }

    public void setNaming(NamingStrategy naming) {
        this.naming = naming;
    }


}
