
public class Persona {
	private String dni;
	private String nombre;
	private int edad;
	private Cuenta miCuenta;

	public Persona(String dni) {
		this.dni = dni;
	}

	public Persona(String dni, String nombre, int edad) {
		this.dni = dni;
		this.nombre = nombre;
		this.edad = edad;
	}

	public Persona(String dni, Cuenta miCuenta) {
		super();
		this.dni = dni;
		this.miCuenta = miCuenta;
	}
	public Persona(String dni, String nombre, int edad, Cuenta miCuenta) {
		super();
		this.dni = dni;
		this.nombre = nombre;
		this.edad = edad;
		this.miCuenta = miCuenta;
	}

	//
	public Persona() {

	}

	

	@Override
	public String toString() {
		return "Persona [dni=" + dni + ", nombre=" + nombre + ", edad=" + edad + ", miCuenta=" + miCuenta + "]";
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public int getEdad() {
		return edad;
	}

	public void setEdad(int edad) {
		this.edad = edad;
	}

	
	public Cuenta getMiCuenta() {
		return miCuenta;
	}

	public void setMiCuenta(Cuenta miCuenta) {
		this.miCuenta = miCuenta;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dni == null) ? 0 : dni.hashCode());
		result = prime * result + edad;
		result = prime * result + ((nombre == null) ? 0 : nombre.hashCode());
		return result;
	}

	public String getDni() {
		return dni;
	}

	public void setDni(String dni) {
		this.dni = dni;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Persona other = (Persona) obj;
		if (dni == null) {
			if (other.dni != null)
				return false;
		} else if (!dni.equals(other.dni))// dos personas con el mismo dni
			return false;
		return true;
	}

}
