package pers.clare.hisql;

import pers.clare.hisql.function.ResultSetValueConverter;
import pers.clare.hisql.naming.NamingStrategy;
import pers.clare.hisql.page.PageMode;

import java.util.HashMap;
import java.util.Map;

public class HiSqlContext {
    private static final Map<Class<?>, ResultSetValueConverter> resultSetValueConverterMap = new HashMap<>();

    private String xmlRoot = "sqlquery/";

    private PageMode pageMode;

    private NamingStrategy naming;

    public static void addResultSetValueConverter(Class<?> clazz, ResultSetValueConverter resultSetValueConverter) {
        resultSetValueConverterMap.put(clazz, resultSetValueConverter);
    }

    public static ResultSetValueConverter getResultSetValueConverter(Class<?> clazz) {
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
