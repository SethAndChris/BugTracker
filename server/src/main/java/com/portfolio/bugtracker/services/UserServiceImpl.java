package com.portfolio.bugtracker.services;


import com.portfolio.bugtracker.models.*;
import com.portfolio.bugtracker.repositories.RoleRepository;
import com.portfolio.bugtracker.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements UserService Interface
 */
@Transactional
@Service(value = "userService")
public class UserServiceImpl
		implements UserService
{
	/**
	 * Connects this service to the User table.
	 */
	@Autowired
	private UserRepository userrepos;
	
	@Autowired
	private RoleRepository rolerepos;

	@Autowired
	private CompanyService companyService;

	@Autowired
	private TicketService ticketService;
	
	@Override
	public User findByName(String name)
	{
		User uu = userrepos.findByUsername(name.toLowerCase());
		if (uu == null)
		{
			throw new EntityNotFoundException("User name " + name + " not found!");
		}
		return uu;
	}
	
	@Override
	public User save(User user) throws Exception
	{
		User newUser = new User();
		
		if (user.getUserid() != 0)
		{
			userrepos.findById(user.getUserid())
					.orElseThrow(() -> new EntityNotFoundException("User id " + user.getUserid() + " not found!"));
			newUser.setUserid(user.getUserid());
		}
		
		newUser.setUsername(user.getUsername()
				.toLowerCase());
		newUser.setPassword(user.getPassword());
		newUser.setEmail(user.getEmail());
		
		newUser.getRoles()
				.clear();
		for (UserRoles ur : user.getRoles())
		{
			Role addRole = rolerepos.findById(ur.getRole()
					.getRoleid())
					.orElseThrow(() -> new EntityNotFoundException("Role id " + ur.getRole()
							.getRoleid() + " not found!"));
			newUser.getRoles()
					.add(new UserRoles(newUser,
							addRole));
		}

		newUser.getTickets().clear();
		for (UserTickets ut : user.getTickets())
		{
			Ticket ticket = ticketService.findTicketById(ut.getTicket().getTicketid());
			newUser.getTickets().add(new UserTickets(newUser, ticket));
		}

		newUser.setCompany(user.getCompany());
		
		return userrepos.save(newUser);
	}

    @Override
    public void deleteAllUsers()
    {
        userrepos.deleteAll();
    }

	@Override
	public User findUserById(long userid)
	{
		User user = userrepos.findById(userid).orElseThrow(() -> new EntityNotFoundException("User not found!"));
		return user;
	}

    @Override
    public User findByUsername(String name)
    {
    	User user = userrepos.findByUsername(name);
        return user;
    }

    @Override
    public List<User> findAllUsers()
    {
    	List<User> userList = new ArrayList<>();
    	userrepos.findAll().iterator().forEachRemaining(userList::add);

        return userList;
    }

	@Override
	public User edit(User partiallyEditedUser) throws Exception
	{
		if (partiallyEditedUser.getUserid() == 0)
		{
			throw new Exception("You cannot patch a user that hasn't been created.");
		}

		User editedUser = userrepos.findById(partiallyEditedUser.getUserid())
				.orElseThrow(() -> new EntityNotFoundException("User with id " + partiallyEditedUser.getUserid() + " not found!"));

		if (partiallyEditedUser.getUsername() != null)
		{
			editedUser.setUsername(partiallyEditedUser.getUsername());
		}

		if (partiallyEditedUser.getPassword() != null)
		{
			editedUser.setPassword(partiallyEditedUser.getPassword());
		}

		if (partiallyEditedUser.getEmail() != null)
		{
			editedUser.setEmail(partiallyEditedUser.getEmail());
		}

		//DOESN'T WORK FOR PATCH OR PUT EDITING COMPANY
		if (partiallyEditedUser.getCompany() != null)
		{
			Company company = companyService.findCompanyById(partiallyEditedUser.getCompany().getCompanyid());
			editedUser.setCompany(company);
		}

		//DOESN'T WORK FOR PATCH OR PUT EDITING ROLES
		if (partiallyEditedUser.getRoles().size() > 0)
		{
			for (UserRoles ur : partiallyEditedUser.getRoles())
			{
				Role role = rolerepos.findById(ur.getRole().getRoleid())
						.orElseThrow(() -> new EntityNotFoundException("Role id " + ur.getRole().getRoleid() + " not found!"));
				editedUser.getRoles().add(new UserRoles(editedUser, role));
			}
		}

		if (partiallyEditedUser.getTickets().size() > 0)
		{
			for (UserTickets ut : partiallyEditedUser.getTickets())
			{
				Ticket t = ticketService.findTicketById(ut.getTicket().getTicketid());
				editedUser.getTickets().add(new UserTickets(editedUser, t));
			}
		}

		return userrepos.save(editedUser);
	}

	@Override
	public void deleteUserById(long userid)
	{
		userrepos.deleteById(userid);
	}

	@Override
	public List<User> fetchUsersByCompany(long companyid)
	{
		List<User> userList = userrepos.findUserByCompany(companyid);

		return userList;
	}

	//both limited methods return fields that aren't specified, so we may need to do a custom sql query, but for now this is fine.
    @Override
    public List<User> findAllUsersLimited()
    {
    	List<User> userList = new ArrayList<>();
		userrepos.findAll().iterator().forEachRemaining(userList::add);

		List<User> rtnUserList = new ArrayList<>();
		for (User u : userList)
		{
			User limitedUser = new User();
			limitedUser.setUsername(u.getUsername());
			limitedUser.setEmail(u.getEmail());

			rtnUserList.add(limitedUser);
		}

        return rtnUserList;
    }

	@Override
	public User findUserByIdLimited(long userid)
	{
		User u = userrepos.findById(userid)
				.orElseThrow(() -> new EntityNotFoundException("User with id " + userid + " not found!"));
		User rtnUser = new User();
		rtnUser.setUsername(u.getUsername());
		rtnUser.setEmail(u.getEmail());

		return rtnUser;
	}
}
