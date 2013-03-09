class RoleTest extends AbstractWebTest {

	void testRoleListNewDelete() {
		get '/testRole'
		assertContentContains 'Home'
	}
}
