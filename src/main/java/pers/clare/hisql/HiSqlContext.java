package pers.clare.hisql;

import pers.clare.hisql.function.ResultSetValueConverter;
import pers.clare.hisql.naming.NamingStrategy;
import pers.clare.hisql.page.PageMode;

import java.util.HashMap;
import java.util.Map;

public class HiSqlContext {

    private String xmlRoot = "sqlquery/";

    private PageMode pageMode;

    private NamingStrategy naming;

    private Map<Class<?>, ResultSetValueConverter> resultSetValueConverterMap = new HashMap<>();

    public void addResultSetValueConverter(Class<?> clazz, ResultSetValueConverter resultSetValueConverter) {
        resultSetValueConverterMap.put(clazz, resultSetValueConverter);
    }

    public ResultSetValueConverter getResultSetValueConverter(Class<?> clazz) {
        return resultSetValueConverterMap.get(clazz);
    }

    public String getXmlRoot() {
        return xmlRoot;
    }

    public void setXmlRoot(String xmlRoot) {
        this.xmlRoot = xmlRoot;
    }

    public PageMode getPageMode() {
        return pageMode;
    }

    public void setPageMode(PageMode pageMode) {
        this.pageMode = pageMode;
    }

    public void setNaming(NamingStrategy naming) {
        this.naming = naming;
    }

    public NamingStrategy getNaming() {
        return naming;
    }


}
