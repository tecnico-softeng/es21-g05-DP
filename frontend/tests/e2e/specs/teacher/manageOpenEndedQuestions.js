describe('Manage Open Ended Questions Walk-through', () => {
    function validateQuestion(title, content, criteria, regex) {
        cy.get('[data-cy="showQuestionDialog"]')
            .should('be.visible')
            .within(($ls) => {
                cy.get('.headline').should('contain', title);
                cy.get('span > p').should('contain', content);
                cy.get('p > span').should('contain', criteria);
                cy.get('p > span').should('contain', regex);
            });
    }

    function validateQuestionFull(title, content, criteria, regex) {
        cy.log('Validate question with show dialog.');

        cy.get('[data-cy="questionTitleGrid"]').first().click();

        validateQuestion(title, content, criteria, regex);

        cy.get('button').contains('close').click();
    }

    before(() => {
        cy.cleanCodeFillInQuestionsByName('Cypress Question Example');
        cy.cleanOpenEndedQuestionsByName('Cypress Question Example');
        cy.cleanMultipleChoiceQuestionsByName('Cypress Question Example');
    });
    after(() => {
        cy.cleanOpenEndedQuestionsByName('Cypress Question Example');
    });

    beforeEach(() => {
        cy.demoTeacherLogin();
        cy.route('GET', '/questions/courses/*').as('getQuestions');
        cy.route('GET', '/topics/courses/*').as('getTopics');
        cy.get('[data-cy="managementMenuButton"]').click();
        cy.get('[data-cy="questionsTeacherMenuButton"]').click();

        cy.wait('@getQuestions').its('status').should('eq', 200);

        cy.wait('@getTopics').its('status').should('eq', 200);
    });

    afterEach(() => {
        cy.logout();
    });

    it('Creates a new open ended question', function () {
        cy.get('button').contains('New Question').click();

        cy.get('[data-cy="createOrEditQuestionDialog"]')
            .parent()
            .should('be.visible');

        cy.get('span.headline').should('contain', 'New Question');

        cy.get(
            '[data-cy="questionTitleTextArea"]'
        ).type('Cypress Question Example - 01', { force: true });
        cy.get('[data-cy="questionQuestionTextArea"]').type(
            'Cypress Question Example - Content - 01',
            {
                force: true,
            }
        );

        cy.get('[data-cy="questionTypeInput"]')
            .type('open_ended', { force: true })
            .click({ force: true });

        cy.wait(1000);

        cy.get('[data-cy="questionCriteriaInput"]').type('This is a criteria', {
            force: true,
        });

        cy.get('[data-cy="questionRegexInput"]').type('This is a regex', {
            force: true,
        });


        cy.route('POST', '/questions/courses/*').as('postQuestion');

        cy.get('button').contains('Save').click();

        cy.wait('@postQuestion').its('status').should('eq', 200);

        cy.get('[data-cy="questionTitleGrid"]')
            .first()
            .should('contain', 'Cypress Question Example - 01');

        validateQuestionFull(
            'Cypress Question Example - 01',
            'Cypress Question Example - Content - 01',
            'This is a criteria',
            'This is a regex'
        );
    });

    it('Can view question (with button)', function () {
        cy.get('tbody tr')
            .first()
            .within(($list) => {
                cy.get('button').contains('visibility').click();
            });

        cy.wait(1000);

        validateQuestion(
            'Cypress Question Example - 01',
            'Cypress Question Example - Content - 01',
            'This is a criteria',
            'This is a regex'
        );

        cy.get('button').contains('close').click();
    });

    it('Can view question (with click)', function () {
        cy.get('[data-cy="questionTitleGrid"]').first().click();

        cy.wait(1000); //making sure codemirror loaded

        validateQuestion(
            'Cypress Question Example - 01',
            'Cypress Question Example - Content - 01',
            'This is a criteria',
            'This is a regex'
        );

        cy.get('button').contains('close').click();
    });
    it('Can update title (with right-click)', function () {
        cy.route('PUT', '/questions/*').as('updateQuestion');

        cy.get('[data-cy="questionTitleGrid"]').first().rightclick();

        cy.wait(1000); //making sure codemirror loaded

        cy.get('[data-cy="createOrEditQuestionDialog"]')
            .parent()
            .should('be.visible')
            .within(($list) => {
                cy.get('span.headline').should('contain', 'Edit Question');

                cy.get('[data-cy="questionTitleTextArea"]')
                    .clear({ force: true })
                    .type('Cypress Question Example - 01 - Edited', { force: true });

                cy.get('button').contains('Save').click();
            });

        cy.wait('@updateQuestion').its('status').should('eq', 200);

        cy.get('[data-cy="questionTitleGrid"]')
            .first()
            .should('contain', 'Cypress Question Example - 01 - Edited');

        validateQuestionFull(
            'Cypress Question Example - 01 - Edited',
            'Cypress Question Example - Content - 01',
            'This is a criteria',
            'This is a regex'

        );
    });

    it('Can update content (with button)', function () {
        cy.route('PUT', '/questions/*').as('updateQuestion');

        cy.get('tbody tr')
            .first()
            .within(($list) => {
                cy.get('button').contains('edit').click();
            });

        cy.wait(1000); //making sure codemirror loaded

        cy.get('[data-cy="createOrEditQuestionDialog"]')
            .parent()
            .should('be.visible')
            .within(($list) => {
                cy.get('span.headline').should('contain', 'Edit Question');

                cy.get('[data-cy="questionQuestionTextArea"]')
                    .clear({ force: true })
                    .type('Cypress New Content For Question!', { force: true });

                cy.get('button').contains('Save').click();
            });

        cy.wait('@updateQuestion').its('status').should('eq', 200);

        validateQuestionFull(
            (title = 'Cypress Question Example - 01 - Edited'),
            (content = 'Cypress New Content For Question!'),
            (criteria = 'This is a criteria'),
            (regex = 'This is a regex')
        );
    });

    it('Can update criteria (with button)', function () {
        cy.route('PUT', '/questions/*').as('updateQuestion');

        cy.get('tbody tr')
            .first()
            .within(($list) => {
                cy.get('button').contains('edit').click();
            });

        cy.wait(1000); //making sure codemirror loaded

        cy.get('[data-cy="createOrEditQuestionDialog"]')
            .parent()
            .should('be.visible')
            .within(($list) => {
                cy.get('span.headline').should('contain', 'Edit Question');

                cy.get('[data-cy="questionCriteriaInput"]')
                    .clear({ force: true })
                    .type('This is a NEW criteria', { force: true });

                cy.get('button').contains('Save').click();
            });

        cy.wait('@updateQuestion').its('status').should('eq', 200);

        validateQuestionFull(
            'Cypress Question Example - 01 - Edited',
            'Cypress New Content For Question!',
            'This is a NEW criteria',
            'This is a regex'
        );
    });

    it('Can update regex (with button)', function () {
        cy.route('PUT', '/questions/*').as('updateQuestion');

        cy.get('tbody tr')
            .first()
            .within(($list) => {
                cy.get('button').contains('edit').click();
            });

        cy.wait(1000); //making sure codemirror loaded

        cy.get('[data-cy="createOrEditQuestionDialog"]')
            .parent()
            .should('be.visible')
            .within(($list) => {
                cy.get('span.headline').should('contain', 'Edit Question');

                cy.get('[data-cy="questionRegexInput"]')
                    .clear({ force: true })
                    .type('This is a NEW regex', { force: true });

                cy.get('button').contains('Save').click();
            });

        cy.wait('@updateQuestion').its('status').should('eq', 200);

        validateQuestionFull(
            'Cypress Question Example - 01 - Edited',
            'Cypress New Content For Question!',
            'This is a NEW criteria',
            'This is a NEW regex'
        );
    });
    it('Can update both criteria and regex (with button)', function () {
        cy.route('PUT', '/questions/*').as('updateQuestion');

        cy.get('tbody tr')
            .first()
            .within(($list) => {
                cy.get('button').contains('edit').click();
            });

        cy.wait(1000); //making sure codemirror loaded

        cy.get('[data-cy="createOrEditQuestionDialog"]')
            .parent()
            .should('be.visible')
            .within(($list) => {
                cy.get('span.headline').should('contain', 'Edit Question');

                cy.get('[data-cy="questionRegexInput"]')
                    .clear({ force: true })
                    .type('This is a NEW NEW regex', { force: true });

                cy.get('[data-cy="questionCriteriaInput"]')
                    .clear({ force: true })
                    .type('This is a NEW NEW criteria', { force: true });

                cy.get('button').contains('Save').click();
            });

        cy.wait('@updateQuestion').its('status').should('eq', 200);

        validateQuestionFull(
            'Cypress Question Example - 01 - Edited',
            'Cypress New Content For Question!',
            'This is a NEW NEW criteria',
            'This is a NEW NEW regex'
        );
    });

    it('Can duplicate a question', function () {
        cy.get('tbody tr')
            .first()
            .within(($list) => {
                cy.get('button').contains('cached').click();
            });

        cy.wait(1000); //making sure codemirror loaded
        cy.get('span.headline').should('contain', 'New Question');

        cy.get(
            '[data-cy="questionTitleTextArea"]'
        ).type('{end} - Duplicate', { force: true });
        cy.get('[data-cy="questionQuestionTextArea"]').type(
            '{end} - Duplicate',
            {
                force: true,
            }
        );

        cy.get('[data-cy="questionCriteriaInput"]').type('{end} - Duplicate', {
            force: true,
        });

        cy.get('[data-cy="questionRegexInput"]').type('{end} - Duplicate', {
            force: true,
        });

        cy.route('POST', '/questions/courses/*').as('postQuestion');

        cy.get('button').contains('Save').click();

        cy.wait('@postQuestion').its('status').should('eq', 200);

        cy.get('[data-cy="questionTitleGrid"]')
            .first()
            .should('contain', 'Cypress Question Example - 01 - Edited - Duplicate');

        cy.wait(1000);

        validateQuestionFull(
            'Cypress Question Example - 01 - Edited - Duplicate',
            'Cypress New Content For Question! - Duplicate',
            'This is a NEW NEW criteria - Duplicate',
            'This is a NEW NEW regex - Duplicate'
        );
    });

    it('Can delete an Open Ended Question',  function () {
        cy.route('DELETE', '/questions/*').as('deleteQuestion');
        cy.get('tbody tr')
            .first()
            .within(($list) => {
                cy.get('button').contains('delete').click();
            });
        cy.wait('@deleteQuestion').its('status').should('eq', 200);
    });
});


