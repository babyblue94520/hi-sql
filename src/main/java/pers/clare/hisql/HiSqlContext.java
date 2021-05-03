package pers.clare.hisql;

import pers.clare.hisql.function.ResultSetValueConverter;
import pers.clare.hisql.naming.NamingStrategy;
import pers.clare.hisql.page.PaginationMode;

import java.util.HashMap;
import java.util.Map;

public class HiSqlContext {
    private static final Map<Class<?>, ResultSetValueConverter> resultSetValueConverterMap = new HashMap<>();

    private String xmlRoot;

    private PaginationMode paginationMode;

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

    public PaginationMode getPaginationMode() {
        return paginationMode;
    }

    public void setPaginationMode(PaginationMode paginationMode) {
        this.paginationMode = paginationMode;
    }

    public void setNaming(NamingStrategy naming) {
        this.naming = naming;
    }

    public NamingStrategy getNaming() {
        return naming;
    }


}
