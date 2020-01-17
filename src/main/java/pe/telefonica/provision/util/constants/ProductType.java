package pe.telefonica.provision.util.constants;

public enum ProductType {

	DUO_BA("DUO BA", true, true, false),
	MOVISTAR_UNO("MOVISTAR UNO", true, true, true),
	MONO_BA("MONO BA", true, false, false),
	LINEA("LINEA", false, true, false),
	INGRESADO("INGRESADO", false, false, false),
	MONO_TV("MONO TV", false, false, true),
	DUO_TV("DUO TV", false, true, true),
	TUP("INGRESADO", false, false, false),
	SVAS("SVAS", false, false, false),
	MOVISTAR_UNO_CATV("MOVISTAR UNO CATV", true, true, true),
	EQUIPOS("EQUIPOS", false, false, false),
	CABLE("CABLE", false, false, true),
	PAGINAS_BLANCAS("PAGINAS BLANCAS", false, false, false),
	SVA_LINEA("SVA_LINEA", false, true, false),
	SVA_LINEA2("SVA LINEA", false, true, false),
	TRIO("TRIO", true, true, true),
	BLOQUE_DE_PRODUCTO("BLOQUE DE PRODUCTO", false, false, true),
	MONO_LINEA("MONO LINEA", false, true, false),
	DECO_SMART_HD("DECO SMART HD", false, false, true),
	TRIOS_DTH("TRIOS DTH", true, true, true),
	TRIOS("TRIOS", true, true, true),
	DUO_INT_TV("DUO INT + TV", true, false, true),
	DUO("DUO", true, true, false),
	BANDA_ANCHA("BANDA ANCHA", true, false, false),
	BLOQUE_TV("BLOQUE TV", false, false, true),
	MONOPRODUCTO("MONOPRODUCTO", false, false, false),
	PLAN_MULTIDESTINO("PLAN MULTIDESTINO", false, true, false);

	private String typeName;
	private boolean internet;
	private boolean line;
	private boolean tv;

	private ProductType(String typeName, boolean internet, boolean line, boolean tv) {
		this.typeName = typeName;
		this.internet = internet;
		this.line = line;
		this.tv = tv;
	}
	
	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public boolean isInternet() {
		return internet;
	}

	public void setInternet(boolean internet) {
		this.internet = internet;
	}

	public boolean isLine() {
		return line;
	}

	public void setLine(boolean line) {
		this.line = line;
	}

	public boolean isTv() {
		return tv;
	}

	public void setTv(boolean tv) {
		this.tv = tv;
	}

}
