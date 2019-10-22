package pe.telefonica.provision.util.constants;

import java.util.HashMap;
import java.util.Map;

public enum ErrorCode {
	
	E2100("201ERR10"),
	E2101("201ERR11"),
	E2102("201ERR02"),
	E2103("201ERR09"),
	E2104("201ERR16"),
	E2105("201ERR17"),
	E2106("201ERR15"),
	E2107("201ERR06"),
	E2108("201ERR03"),
	E2109("201ERR04"),
	E2110("201ERR18"),
	E2111("201ERR05"),
	E2112("202ERR10"),
	E2113("202ERR11"),
	E2114("202ERR02"),
	E2115("202ERR09"),
	E2116("202ERR15"),
	E2117("202ERR06"),
	E2118("202ERR03"),
	E2119("202ERR04"),
	E2120("202ERR18"),
	E2121("202ERR05"),
	E2122("203ERR10"),
	E2123("203ERR11"),
	E2124("203ERR02"),
	E2125("203ERR15"),
	E2126("203ERR03"),
	E2127("203ERR19"),
	E2128("203ERR18"),
	E2129("203ERR05"),
	E2130("204ERR10"),
	E2131("204ERR11"),
	E2132("204ERR02"),
	E2133("204ERR15"),
	E2134("204ERR03"),
	E2135("204ERR19"),
	E2136("204ERR18"),
	E2137("204ERR05");
	
	private String externalCode;
	 
	ErrorCode(String extCode) {
        this.externalCode = extCode;
    }
 
    public String getExternalCode() {
        return externalCode;
    }
     
    //****** Reverse Lookup Implementation************//
    //Lookup table
    private static final Map<String, ErrorCode> lookup = new HashMap<>();
  
    //Populate the lookup table on loading time
    static
    {
        for(ErrorCode err : ErrorCode.values()){
            lookup.put(err.getExternalCode(), err);
        }
    }
  
    //This method can be used for reverse lookup purpose
    public static ErrorCode get(String extCode)
    {
        return lookup.get(extCode);
    }

}
