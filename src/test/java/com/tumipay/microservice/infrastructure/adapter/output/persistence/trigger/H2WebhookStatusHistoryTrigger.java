package com.tumipay.microservice.infrastructure.adapter.output.persistence.trigger;

import org.h2.api.Trigger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

/**
 * H2-compatible trigger to register webhook status transitions in history table.
 */
public class H2WebhookStatusHistoryTrigger implements Trigger {

    private static final int IDX_PWE_ID = 0;
    private static final int IDX_PWE_UUID = 1;
    private static final int IDX_PWE_EVENT_TYPE = 3;
    private static final int IDX_PWE_PROCESSING_STATUS = 6;
    private static final int IDX_PWE_ERROR_CODE = 7;
    private static final int IDX_PWE_RETRY_COUNT = 8;
    private static final int IDX_PWE_CLAIMED_BY = 14;

    @Override
    public void init(
            Connection connection,
            String schemaName,
            String triggerName,
            String tableName,
            boolean before,
            int type
    ) {
        // No-op.
    }

    @Override
    public void fire(Connection connection, Object[] oldRow, Object[] newRow) throws SQLException {
        if (oldRow == null || newRow == null) {
            return;
        }

        Object oldStatus = oldRow[IDX_PWE_PROCESSING_STATUS];
        Object newStatus = newRow[IDX_PWE_PROCESSING_STATUS];

        // Match PostgreSQL IS DISTINCT FROM behavior.
        if (Objects.equals(oldStatus, newStatus)) {
            return;
        }

        final String sql = """
                INSERT INTO tp_provider_webhook_event_history (
                    pweh_pwe_id,
                    pweh_pwe_uuid,
                    pweh_event_type,
                    pweh_old_status,
                    pweh_new_status,
                    pweh_error_code,
                    pweh_retry_count,
                    pweh_claimed_by,
                    pweh_changed_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, oldRow[IDX_PWE_ID]);
            statement.setObject(2, toUuid(oldRow[IDX_PWE_UUID]));
            statement.setString(3, toStringValue(oldRow[IDX_PWE_EVENT_TYPE]));
            statement.setString(4, toStringValue(oldStatus));
            statement.setString(5, toStringValue(newStatus));
            statement.setString(6, toStringValue(newRow[IDX_PWE_ERROR_CODE]));
            statement.setInt(7, toIntValue(newRow[IDX_PWE_RETRY_COUNT]));
            statement.setString(8, toStringValue(newRow[IDX_PWE_CLAIMED_BY]));
            statement.executeUpdate();
        } catch (SQLException ignored) {
            // Keep update flow resilient for tests, equivalent to PG function exception handling.
        }
    }

    @Override
    public void close() {
        // No-op.
    }

    @Override
    public void remove() {
        // No-op.
    }

    private static UUID toUuid(Object value) {
        if (value instanceof UUID uuid) {
            return uuid;
        }
        if (value == null) {
            return null;
        }
        return UUID.fromString(String.valueOf(value));
    }

    private static String toStringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static int toIntValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return 0;
        }
        return Integer.parseInt(String.valueOf(value));
    }
}

