/*
 * Copyright (C) 2005-2007 Les Hazlewood, Jeremy Haile
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the
 *
 * Free Software Foundation, Inc.
 * 59 Temple Place, Suite 330
 * Boston, MA 02111-1307
 * USA
 *
 * Or, you may view it online at
 * http://www.opensource.org/licenses/lgpl-license.php
 */
package org.jsecurity.realm;

import org.jsecurity.authc.module.AuthenticationModule;
import org.jsecurity.authz.AuthorizationException;

import java.security.Permission;
import java.security.Principal;
import java.util.Collection;
import java.util.List;

/**
 * A <tt>Realm</tt> is a security component that can access application-specific security entities
 * such as users, roles, and permissions to determine authorization behavior.
 *
 * <p><tt>Realm</tt>s usually have a 1-to-1 correspondance with a datasource such as a relational database,
 * file sysetem, or other similar resource.  As such, implementations of this interface use datasource-specific APIs to
 * determine authorization information, such as JDBC, File IO, Hibernate or JPA, or any other Data Access API.
 *
 * <p>Because most of these datasources usually contain subject (user) information such as usernames and passwords,
 * this
 * interface also extends the {@link AuthenticationModule} interface for convenience, thereby allowing a Realm to act
 * as a pluggable authentication module in a PAM configuration.  This allows a Realm to perform <i>both</i>
 * authentication and authorization duties for a single datasource, which caters to 90% of the use cases of most
 * applications.  If for some reason you don't want your Realm implementation to perform authentication duties, you
 * should override the {@link AuthenticationModule#supports(Class)} method to always return <tt>false</tt>.
 *
 * <p>Because every application is different, security data such as users and roles can be
 * represented in any number of ways.  Because JSecurity tries to
 * maintain a non-intrusive development philosophy whenever possible, it does not require you to
 * implement or extend any <tt>User</tt>, <tt>Group</tt> or <tt>Role</tt> interfaces or classes.
 *
 * <p>Instead, JSecurity allows applications to implement this interface to access
 * environment-specific datasources and data model objects.  The implementation can then be
 * plugged in to the application's JSecurity configuration.  This modular technique abstracts
 * away any environment/modeling details and allows JSecurity to be deployed in
 * practically any application environment.
 *
 * @see AuthenticationModule
 * @see org.jsecurity.authc.module.support.ModularAuthenticator ModularAuthenticator
 *
 * @since 0.1
 * @author Les Hazlewood
 * @author Jeremy Haile
 */
public interface Realm extends AuthenticationModule {

    /**
     * Returns the name of this Realm used to uniquely identify it within the application.
     * This name must be unique amongst all realms used within this security context.
     * @return the unique identifier for this realm.
     */
    String getName();

    /**
     * Returns <tt>true</tt> if the subject with the id of <tt>subjectIdentifier</tt> has the role
     * with the id of <tt>roleIdentifier</tt>, <tt>false</tt> otherwise.
     *
     * <p>In most systems, the <tt>subjectIdentifier</tt> is usually a <tt>Principal</tt>
     * representing a <tt>User</tt> database primary
     * key or a String username, and the <tt>roleIdentifier</tt> is usually a <tt>Role</tt> entity's
     * primary key or a String role name.
     *
     * <p>The runtime values of the method arguments are specific to the application and
     * determined by the application's JSecurity configuration.
     *
     * @param subjectIdentifier a <tt>Principal</tt> representing the application-specific identifier
     * for the subject to check for role association (usually a user id or username).
     * @param roleIdentifier the application-specific identifier of the role to check for
     * association with a subject (usually a role id or role name ).
     * @return <tt>true</tt> if the subject with the id of <tt>subjectIdentifier</tt> has the role
     * with the id of <tt>roleIdentifier</tt>, <tt>false</tt> otherwise.
     */
    boolean hasRole( Principal subjectIdentifier, String roleIdentifier );

    /**
     * Checks to see if the roles with the given identifiers are associated with the subject (user)
     * with the given identifier and returns a boolean array indicating which
     * roles are associated with the given subject.
     *
     * <p>This is primarily a performance-enhancing method to help reduce the number of
     * {@link #hasRole} invocations over the wire in client/server systems.
     *
     * <p>In most systems, the <tt>subjectIdentifier</tt> is usually a <tt>Principal</tt>
     * representing a <tt>User</tt> database primary
     * key or a String username, and <tt>roleIdentifiers</tt> is usually a List of <tt>Role</tt>
     * entity primary keys or String role names.
     *
     * <p>The runtime values of the method arguments are specific to the application and
     * determined by the application's JSecurity configuration.
     *
     * @param subjectIdentifier a <tt>Principal</tt> representing the application-specific identifier
     * for the subject to check for role association (usually a user id or username).
     * @param roleIdentifiers the identifiers of the roles to check for.
     * @return an array of booleans whose indices correspond to the index of the
     * roles in the given identifiers.  A true value indicates the user has the
     * role at that index.  False indicates the user does not have the role.
     */
    boolean[] hasRoles( Principal subjectIdentifier, List<String> roleIdentifiers );

    /**
     * Returns <tt>true</tt> if the subject with the given <tt>subjectIdentifier</tt> has all the
     * roles with the given identifiers, <tt>false</tt> otherwise.
     *
     * <p>In most systems, the <tt>subjectIdentifier</tt> is usually a <tt>Principal</tt>
     * representing a <tt>User</tt> database primary
     * key or a String username, and <tt>roleIdentifiers</tt> is usually a List of <tt>Role</tt>
     * entity primary keys or a String role names.
     *
     * <p>The runtime values of the method arguments are specific to the application and
     * determined by the application's JSecurity configuration.
     *
     * @param subjectIdentifier a <tt>Principal</tt> representing the application-specific identifier
     * for the subject to check for role association (usually a user id or username).
     * @param roleIdentifiers the roles to be checked.
     * @return true if the user has all roles, false otherwise.
     */
    boolean hasAllRoles( Principal subjectIdentifier, Collection<String> roleIdentifiers );

    /**
     * Returns <tt>true</tt> if the subject with the given <tt>subjectIdentifier</tt> is
     * permitted to perform an action or access a resource summarized by the specified permission.
     *
     * <p>More specifically, this method should determine if any <tt>Permission</tt>s associated
     * with the subject {@link Permission#implies(java.security.Permission) imply} the
     * specified permission.
     *
     * <p>In most systems, the <tt>subjectIdentifier</tt> is usually a <tt>Principal</tt>
     * representing a <tt>User</tt> database primary
     * key or a String username.  The runtime value of the <tt>subjectIdentifier</tt>
     * is specific to the application and
     * determined by the application's JSecurity configuration.
     *
     * @param subjectIdentifier a <tt>Principal</tt> representing the application-specific identifier
     * for the subject to check (usually a user id or username).
     * @param permission the permission that is being checked.
     * @return true if the user associated with this context is permitted, false otherwise.
     */
    boolean isPermitted( Principal subjectIdentifier, Permission permission );

    /**
     * Checks a collection of permissions to see if they are associated with the subject with
     * the given <tt>subjectIdentifier</tt> and and returns a boolean array indicating which
     * permissions are associated with the subject.
     *
     * <p>More specifically, this method should determine if each <tt>Permission</tt> in
     * the array is {@link Permission#implies(java.security.Permission) implied by} permissions
     * already associated with the subject.
     *
     * <p>This is primarily a performance-enhancing method to help reduce the number of
     * {@link #isPermitted} invocations over the wire in client/server systems.
     *
     * <p>In most systems, the <tt>subjectIdentifier</tt> is usually a <tt>Principal</tt>
     * representing a <tt>User</tt> database primary
     * key or a String username.  The runtime value of the <tt>subjectIdentifier</tt>
     * is specific to the application and
     * determined by the application's JSecurity configuration.
     *
     * @param subjectIdentifier a <tt>Principal</tt> representing the application-specific identifier
     * for the subject to check (usually a user id or username).
     * @param permissions the permissions to check for.
     * @return an array of booleans whose indices correspond to the index of the
     * permissions in the given list.  A true value at an index indicates the user is permitted for
     * for the associated <tt>Permission</tt> object in the list.  A false value at an index
     * indicates otherwise.
     */
    boolean[] isPermitted( Principal subjectIdentifier, List<Permission> permissions );

    /**
     * Checks if the the subject with the given <tt>subjectIdentifier</tt> implies all the
     * specified permissions.
     *
     * <p>More specifically, this method should determine if <em>all</em> of the given
     * <tt>Permission</tt>s are {@link Permission#implies(java.security.Permission) implied by}
     * permissions already associated with the subject.
     *
     * <p>In most systems, the <tt>subjectIdentifier</tt> is usually a <tt>Principal</tt>
     * representing a <tt>User</tt> database primary
     * key or a String username.  The runtime value of the <tt>subjectIdentifier</tt>
     * is specific to the application and
     * determined by the application's JSecurity configuration.
     * @param subjectIdentifier a <tt>Principal</tt> representing the application-specific identifier
     * for the subject to check (usually a user id or username).
     * @param permissions the permissions to be checked.
     * @return true if the user has all permissions, false otherwise.
     */
    boolean isPermittedAll( Principal subjectIdentifier, Collection<Permission> permissions );


    /**
     * A convenience method to ensure a subject (and/or user)
     * {@link Permission#implies(java.security.Permission) implies} the specified <tt>Permission</tt>.
     * If the subject's exisiting associated permissions do not
     * {@link Permission#implies(java.security.Permission) imply} the given permission,
     * an {@link org.jsecurity.authz.AuthorizationException} will be thrown.
     *
     * <p>In most systems, the <tt>subjectIdentifier</tt> is usually a <tt>Principal</tt>
     * representing a <tt>User</tt> database primary
     * key or a String username.  The runtime value of the <tt>subjectIdentifier</tt>
     * is specific to the application and
     * determined by the application's JSecurity configuration.
     *
     * @param subjectIdentifier a <tt>Principal</tt> representing the application-specific identifier
     * for the subject to check (usually a user id or username).
     * @param permission the permission to check.
     * @throws org.jsecurity.authz.AuthorizationException if the user does not have the permission.
     */
    void checkPermission( Principal subjectIdentifier, Permission permission ) throws AuthorizationException;


    /**
     * A convenience method to ensure a subject (and/or user)
     * {@link Permission#implies(java.security.Permission) implies} <em>all</em> of the
     * specified <tt>Permission</tt>s.
     * If the subject's exisiting associated permissions do not
     * {@link Permission#implies(java.security.Permission) imply} <em>all</em> of the given permissions,
     * an {@link org.jsecurity.authz.AuthorizationException} will be thrown.
     * @param subjectIdentifier a <tt>Principal</tt> representing the application-specific identifier
     * for the subject to check (usually a user id or username).
     * @param permissions the permissions to check.
     * @throws AuthorizationException if the user does not have all of the given
     * permissions.
     */
    void checkPermissions( Principal subjectIdentifier, Collection<Permission> permissions ) throws AuthorizationException;

}