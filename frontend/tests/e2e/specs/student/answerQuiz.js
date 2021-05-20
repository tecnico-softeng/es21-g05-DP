describe('Student walkthrough', () => {
    beforeEach(() => {
        cy.demoTeacherLogin();

        cy.CreateItemCombinationQuestion('Item Combination Question', 'Item Combination Question Content');
        cy.createQuestion(
            'Question Quiz 2',
            'Question',
            'Option',
            'Option',
            'Option',
            'Correct'
        );
          cy.createQuizzWith2Questions(
            'Item Combination Quiz',
            'Item Combination Question',
            'Question Quiz 2'
          );
          cy.contains('Logout').click();
    });
    
    afterEach(() => {
        cy.contains('Logout').click();
    });
    
    it('Student answers a quiz with Item Combination Question and gets the results', () => {
        cy.demoStudentLogin();

        cy.get('[data-cy="quizzesStudentMenuButton"]').click();
        cy.contains('Available').click();
        cy.contains('Item Combination Quiz').click();

        cy.get('[data-cy="Connections 1"]').parent().click();
        cy.wait(2000);
        cy.get(".v-list-item__title").contains('3').click({ force: true });

        cy.get(".v-list-item__title").contains('4').click({ force: true });
        cy.wait(2000);
        cy.get('[data-cy="nextQuestionButton"]').click();
        cy.get('[data-cy="optionList"]').children().eq(1).click();

        cy.get('[data-cy="endQuizButton"]').click();
        cy.get('[data-cy="confirmationButton"]').click();
        cy.wait(2000);

        cy.get('[data-cy="nextQuestionButton"]').click();
        cy.wait(2000);
        
      });
});