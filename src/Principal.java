import java.util.Objects;
import java.util.Scanner;
import java.util.logging.Logger;

import com.db4o.*;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.query.Predicate;
import com.db4o.query.QueryComparator;

public class Principal {

	private static final String BD_PERSONAS = "personas.oo";
	private static Scanner teclado = new Scanner(System.in);

	public static void main(String[] args) {

		int opc;
		ObjectContainer db;

		db = abrirBd();
		do {
			opc = solicitarOpcion();
			tratarOpcion(opc, db);
		} while (opc != 10);

		db.close();

	}

	private static ObjectContainer abrirBd() {
		EmbeddedConfiguration config = Db4oEmbedded.newConfiguration();
		ObjectContainer db = Db4oEmbedded.openFile(config, BD_PERSONAS);
		return db;
	}

	private static ObjectContainer abrirBd(EmbeddedConfiguration configuracion) {

		ObjectContainer db = Db4oEmbedded.openFile(configuracion, BD_PERSONAS);

		return db;
	}

	private static void tratarOpcion(int opc, ObjectContainer db) {
		int edad, numCuenta;
		String dni, nombre;
		double saldo;
		Persona asociarCuentaPersona = null;
		Cuenta cuenta = null;

		switch (opc) {
		case 1:
			insertarPersonaEnBd(db);
			break;

		case 2:
			consultarBd(db);
			break;
		case 3:
			edad = solicitarEdad();
			consultarPersonasConEdad(db, edad);
			break;
		case 4:
			dni = solicitarCadena("Introduce el dni que deseas buscar");
			if (personaPorDni(db, dni) != null) {
				nombre = solicitarCadena("Introduce el nuevo nombre: ");
				edad = solicitarEdad();
				modificarPersonaDni(db, dni, nombre, edad);
			} else {
				System.out.println("Error");
			}

			break;
		case 5:
			System.out.println("Introduce el numero de cuenta: ");
			numCuenta = Integer.parseInt(teclado.nextLine());

			if (consultarNumCuenta(db, numCuenta) == null) {
				System.out.println("Introduce el saldo");
				saldo = Double.parseDouble(teclado.nextLine());

				dni = solicitarCadena("Introduce el dni del propietario para la nueva cuenta");

				Persona p= personaPorDni(db, dni);
				if (p == null) {
					System.out.println("Error, no existe");
				} else {
					cuenta = new Cuenta(numCuenta, saldo);
					p.setMiCuenta(cuenta);
					db.store(p);
					System.out.println("Cuenta actualizada");
				}
			} else {
				System.out.println("Error, esa cuenta ya existe en la bd");
			}
			break;
		case 6:
			dni = solicitarCadena("Introduce el dni de la persona que deseas consultar: ");
			asociarCuentaPersona = personaPorDni(db, dni);

			if (asociarCuentaPersona != null) {
				System.out.println(asociarCuentaPersona.toString());
			} else {
				System.out.println("Error, ese dni no existe " + dni);
			}
			break;
		case 7:
			Persona eliminarPersona;

			// Nivel 2 para que se cargen todas las cuentas
			db.ext().configure().activationDepth(2);
			// Al borrar la persona se borra la cuenta
			db.ext().configure().objectClass(Persona.class).cascadeOnDelete(true);

			dni = solicitarCadena("Introduce el dni de la persona que deseas consultar: ");
			eliminarPersona = personaPorDni(db, dni);

			if (eliminarPersona == null) {
				System.out.println("Error, no existe una persona con ese dni");
			} else {
				System.out.println("¿Está usted seguro de que desea eliminar este registro? (S/N)");
				String respuesta = teclado.nextLine();

				if (respuesta.equalsIgnoreCase("S")) {
					db.delete(eliminarPersona);
				}
			}
			break;
		case 8:
			consultarCuentas(db);
			break;
		case 9:
			// Lista de personas que tengan mas saldo del solicitado
			System.out.println("Introduce el limite de saldo que deseas buscar: ");
			saldo = Double.parseDouble(teclado.nextLine());
			personasConUnDeterminadoSaldo(db, saldo);
			break;
		}

	}

	private static int solicitarOpcion() {
		int opc;
		System.out.println("1.Insertar persona en  BD");
		System.out.println("2.Consutar BD completa");
		System.out.println("3.Consultar personas con una edad");
		System.out.println("4.Modificar datos de una persona por su dni(modificar nombre y edad)");
		System.out.println(
				"5.Asociar una cuenta a una persona. Se solicita el numero de cuenta,saldo y dni de la persona y se le asociara la cuenta");
		System.out.println("6.Consultar una persona por dni(aparecerá su cuenta si tiene)");
		System.out.println("7.Borrar una persona por dni.(que se borre tb su cuenta)");
		System.out.println("8.Consultar cuentas existentes(sald y num cuenta)");
		System.out.println("9.Consultar personas con cuentas que tienen mas de un determinado saldo");
		System.out.println("10.Salir");
		do {
			System.out.println("Introduce opcion");
			opc = Integer.parseInt(teclado.nextLine());
		} while (opc < 1 || opc > 10);
		return opc;
	}

	private static void personasConUnDeterminadoSaldo(ObjectContainer db, double saldo) {
		db.ext().configure().activationDepth(2);

		ObjectSet<Persona> resultado = db.query(new Predicate<Persona>() {

			@Override
			public boolean match(Persona personaConSaldo) {

				return personaConSaldo.getMiCuenta().getSaldo() > saldo;
			}
		}, new QueryComparator<Persona>() {

			@Override
			public int compare(Persona saldo1, Persona saldo2) {
				return Double.compare(saldo1.getMiCuenta().getSaldo(), saldo2.getMiCuenta().getSaldo());
			}
		});

		if (resultado.isEmpty()) {
			System.out.println("No existe un saldo superior al solicitado");
		} else {
			for (Persona persona : resultado) {
				System.out.println(persona);
			}
		}
	}

	private static void borrarPersonaDni(ObjectContainer db, String dni) {
		System.out.println("Introduce el dni de la persona que deseas eliminar: ");
		dni = teclado.nextLine();

		Persona consultarPersona = personaPorDni(db, dni);

		if (consultarPersona == null) {
			System.out.println("No existe ninguna persona con ese dni " + dni);
		} else {
			db.delete(consultarPersona);
			System.out.println("Persona eliminada correctamente");
		}
	}

	private static void consultarBd(ObjectContainer db) {

		Persona patron = new Persona(); // consultar todas las personas, sin filtro

		ObjectSet<Persona> result = db.queryByExample(patron);

		if (result.size() == 0)
			System.out.println("BD Vacia");
		else {
			System.out.println("Numero de personas " + result.size());
			for (Persona persona : result) {

				System.out.println(persona);
			}
		}
	}

	private static void consultarCuentas(ObjectContainer db) {

		Cuenta patron = new Cuenta(); // consultar todas las cuentas, sin filtro

		ObjectSet<Cuenta> result = db.queryByExample(patron);

		if (result.size() == 0)
			System.out.println("BD Vacia");
		else {
			System.out.println("Numero de cuenta " + result.size());
			for (Cuenta cuenta : result) {

				System.out.println(cuenta);
			}
		}
	}

	private static Persona personaPorDni(ObjectContainer db, String dni) {
		Persona persona = null;

		// Le paso el constructor de dni solo para que me lo busque
		Persona personaABuscar = new Persona(dni);
		ObjectSet<Persona> resultado = db.queryByExample(personaABuscar);

		if (resultado.size() == 1) {
			persona = resultado.next();
		}
		return persona;
	}

	private static Cuenta consultarNumCuenta(ObjectContainer db, int numCuenta) {
		Cuenta comprobarCuenta = null;

		Cuenta buscarNumCuenta = new Cuenta(numCuenta);
		ObjectSet<Cuenta> resultado = db.queryByExample(buscarNumCuenta);

		if (resultado.size() == 1) {
			comprobarCuenta = resultado.next();
		}

		return comprobarCuenta;
	}

	private static void modificarPersonaDni(ObjectContainer db, String dni, String nombre, int edad) {

		// db = abrirBd();
		// Busco a la persona por el dni
		Persona modificarPersona = personaPorDni(db, dni);

		if (modificarPersona == null) {
			System.out.println("Error, no existe un registo con ese dni" + dni);
		} else {
			modificarPersona.setNombre(nombre);
			modificarPersona.setEdad(edad);
			db.store(modificarPersona);
		}
		// db.close();
	}

	private static void consultarPersonasConEdad(ObjectContainer db, int edad) {

		Persona patron = new Persona(null, null, edad); // consultar todas las personas que tienen esa edad
		Persona per;

		ObjectSet<Persona> result = db.queryByExample(patron);

		if (result.size() == 0)
			System.out.println("BD Vacia");
		else {
			System.out.println("Numero de personas con edad " + edad + " son: " + result.size());
			while (result.hasNext()) {
				per = result.next();
				System.out.println(per);
			}
		}

	}

	private static void insertarPersonaEnBd(ObjectContainer db) {

		Persona persona = crearPersona();

		db.store(persona);

	}

	private static Persona crearPersona() {
		Persona persona = new Persona(solicitarCadena("Dni:"), solicitarCadena("Nombre: "), solicitarEdad());
		return persona;
	}

	private static int solicitarEdad() {
		int edad;
		do {
			System.out.println("Introduce edad:");
			edad = Integer.parseInt(teclado.nextLine());
		} while (edad < 0);
		return edad;
	}

	private static String solicitarCadena(String msg) {
		String nombre;
		System.out.println(msg);
		nombre = teclado.nextLine();
		return nombre;
	}

}
