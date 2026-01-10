export class PatientDAO {
    private static API_URL = "http://localhost:8080/api/patients";

    // Changement : On ne retourne plus "null" en cas d'erreur, on "throw" (lance) l'erreur
    async login(lastName: string, firstName: string, birthDate: string, isNew: boolean): Promise<number> {
        const data = {
            lastName: lastName,
            firstName: firstName,
            birthDate: birthDate,
            newPatient: String(isNew)
        };

        try {
            const response = await fetch(PatientDAO.API_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });

            // Si le serveur répond une erreur (404, 409, etc.)
            if (!response.ok) {
                // On lit le message JSON envoyé par Java {"error": "..."}
                const errorBody = await response.json();
                throw new Error(errorBody.error || "Erreur serveur");
            }

            // Si tout va bien
            const json = await response.json();
            return json.id;

        } catch (err) {
            // On renvoie l'erreur vers la page de Login pour qu'elle l'affiche
            throw err;
        }
    }
}