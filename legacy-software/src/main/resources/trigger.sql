-- ==========================================
-- LOG TABLE
-- ==========================================

CREATE TABLE IF NOT EXISTS bill_modification_attempts_log (
                                                              id INT AUTO_INCREMENT PRIMARY KEY,
                                                              attempted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                                              source_table ENUM('bills', 'bill_details'),
                                                              record_id INT,
                                                              operation ENUM('UPDATE', 'DELETE'),
                                                              old_data JSON,
                                                              user_name VARCHAR(255)
);
@@

-- ==========================================
-- TRIGGER: Prevent update on bills with status = 1
-- ==========================================

CREATE TRIGGER IF NOT EXISTS prevent_update_bills_status_1
    BEFORE UPDATE ON bills
    FOR EACH ROW
BEGIN
    IF OLD.status = 1 THEN
        INSERT INTO bill_modification_attempts_log (
            source_table, record_id, operation, old_data, user_name
        )
        VALUES (
                   'bills',
                   OLD.id,
                   'UPDATE',
                   JSON_OBJECT('id', OLD.id, 'status', OLD.status),
                   SUBSTRING_INDEX(USER(), '@', 1)
               );

        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot update bill with status 1';
    END IF;
END
@@

-- ==========================================
-- TRIGGER: Prevent delete on bills with status = 1
-- ==========================================

CREATE TRIGGER IF NOT EXISTS prevent_delete_bills_status_1
    BEFORE DELETE ON bills
    FOR EACH ROW
BEGIN
    IF OLD.status = 1 THEN
        INSERT INTO bill_modification_attempts_log (
            source_table, record_id, operation, old_data, user_name
        )
        VALUES (
                   'bills',
                   OLD.id,
                   'DELETE',
                   JSON_OBJECT('id', OLD.id, 'status', OLD.status),
                   SUBSTRING_INDEX(USER(), '@', 1)
               );

        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot delete bill with status 1';
    END IF;
END
@@

-- ==========================================
-- TRIGGER: Prevent update on bill_details if parent bill.status = 1
-- ==========================================

CREATE TRIGGER IF NOT EXISTS prevent_update_bill_details_status_1
    BEFORE UPDATE ON bill_details
    FOR EACH ROW
BEGIN
    DECLARE billStatus INT;

    SELECT status INTO billStatus
    FROM bills
    WHERE id = OLD.bill_id;

    IF billStatus = 1 THEN
        INSERT INTO bill_modification_attempts_log (
            source_table, record_id, operation, old_data, user_name
        )
        VALUES (
                   'bill_details',
                   OLD.id,
                   'UPDATE',
                   JSON_OBJECT('id', OLD.id, 'bill_id', OLD.bill_id),
                   SUBSTRING_INDEX(USER(), '@', 1)
               );

        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Cannot update bill_detail if parent bill has status 1';
    END IF;
END
@@

-- ==========================================
-- TRIGGER: Prevent delete on bill_details if parent bill.status = 1
-- ==========================================

CREATE TRIGGER IF NOT EXISTS prevent_delete_bill_details_status_1
    BEFORE DELETE ON bill_details
    FOR EACH ROW
BEGIN
    DECLARE billStatus INT;

    SELECT status INTO billStatus
    FROM bills
    WHERE id = OLD.bill_id;

    IF billStatus = 1 THEN
        INSERT INTO bill_modification_attempts_log (
            source_table, record_id, operation, old_data, user_name
        )
        VALUES (
                   'bill_details',
                   OLD.id,
                   'DELETE',
                   JSON_OBJECT('id', OLD.id, 'bill_id', OLD.bill_id),
                   SUBSTRING_INDEX(USER(), '@', 1)
               );

        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Cannot delete bill_detail if parent bill has status 1';
    END IF;
END
@@

CREATE TRIGGER IF NOT EXISTS prevent_update_invoices
    BEFORE UPDATE ON supplier_invoices
    FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Cannot update invoice (all invoices are locked)';
END;
@@

-- ==========================================
-- TRIGGER: Prevent DELETE on invoices
-- ==========================================
CREATE TRIGGER IF NOT EXISTS prevent_delete_invoices
    BEFORE DELETE ON supplier_invoices
    FOR EACH ROW
    BEGIN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Cannot delete invoice (all invoices are locked)';
END;
@@

-- ==========================================
-- TRIGGER: Prevent UPDATE on invoice_details (if parent invoice exists)
-- ==========================================
CREATE TRIGGER IF NOT EXISTS prevent_update_invoice_details
    BEFORE UPDATE ON supplier_invoices
    FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Cannot update invoice_detail (all invoice_details are locked)';
END;
@@

-- ==========================================
-- TRIGGER: Prevent DELETE on invoice_details (if parent invoice exists)
-- ==========================================
CREATE TRIGGER IF NOT EXISTS prevent_delete_invoice_details
    BEFORE DELETE ON supplier_invoice_details
    FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot delete invoice_detail (all invoice_details are locked)';
END;
@@