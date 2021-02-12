import React, { useState, useEffect } from 'react';
import axiosWithAuth from '../../utils/axiosWithAuth';
import { useSelector, useDispatch } from 'react-redux';
import Ticket from './Ticket';

export default function CurrentEmployeeTickets() {
    const companyTickets = useSelector(state => state.tickets.tickets)

    return (
        <div>
            {companyTickets.map(ticket => <Ticket ticket={ticket.ticket} />)}
        </div>
    )
}
