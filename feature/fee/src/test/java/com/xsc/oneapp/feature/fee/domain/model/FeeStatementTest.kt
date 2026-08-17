package com.xsc.oneapp.feature.fee.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FeeStatementTest {

    @Test
    fun `a statement with no figures and no lines is empty`() {
        assertTrue(FeeStatement.EMPTY.isEmpty)
    }

    @Test
    fun `a statement carrying only totals is not empty`() {
        assertFalse(FeeStatement("10", 50000.0, 10000.0, 40000.0).isEmpty)
    }

    @Test
    fun `payableAmount is the outstanding balance when something is owed`() {
        assertEquals(40000.0, FeeStatement("10", 50000.0, 10000.0, 40000.0).payableAmount)
    }

    @Test
    fun `a settled account has nothing payable, so no Pay now button`() {
        assertNull(FeeStatement("10", 50000.0, 50000.0, 0.0).payableAmount)
    }

    @Test
    fun `a credit balance is not payable either`() {
        assertNull(FeeStatement("10", 50000.0, 60000.0, -10000.0).payableAmount)
    }
}
