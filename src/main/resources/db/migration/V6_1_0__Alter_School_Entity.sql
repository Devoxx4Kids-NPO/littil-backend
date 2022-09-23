ALTER TABLE school
    ADD CONSTRAINT fk_school_contact_person
       FOREIGN KEY (contact_person)
            REFERENCES contact_person (contact_person_id);