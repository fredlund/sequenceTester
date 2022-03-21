package es.upm.babel.sequenceTester;

/**
 *  Provides information about the library version.
 *  Library version numbers use the scheme Major.Minor.Patch,
 *  where normally a change in the Major number signals an incompatible
 * API changes, a change in the Minor number signals additions to the API
 * while preserving compatibility for older programs, and changes in
 * the Patch number corrects
 * errors in the library while not changing the API (except fixing the bug).
 *
 */
public class Version {
    /**
     * Returns the major version number.
     * @return the major version number.
     */
    public static int major() {
	return 2;
    }

    /**
     * Returns the minor version number.
     * @return the minor version number.
     */
    public static int minor() {
	return 0;
    }
    
    /**
     * Returns the patchlevel revision number.
     * @return the patchlevel revision number.
     */
    public static int patchlevel() {
	return 0;
    }
}
