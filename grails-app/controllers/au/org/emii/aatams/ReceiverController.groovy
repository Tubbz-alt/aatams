package au.org.emii.aatams

class ReceiverController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [receiverInstanceList: Receiver.list(params), receiverInstanceTotal: Receiver.count()]
    }

    def create = {
        def receiverInstance = new Receiver()
        receiverInstance.properties = params
        return [receiverInstance: receiverInstance]
    }

    def save = {
        def receiverInstance = new Receiver(params)
        if (receiverInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'receiver.label', default: 'Receiver'), receiverInstance.id])}"
            redirect(action: "show", id: receiverInstance.id)
        }
        else {
            render(view: "create", model: [receiverInstance: receiverInstance])
        }
    }

    def show = {
        def receiverInstance = Receiver.get(params.id)
        if (!receiverInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'receiver.label', default: 'Receiver'), params.id])}"
            redirect(action: "list")
        }
        else {
            [receiverInstance: receiverInstance]
        }
    }

    def edit = {
        def receiverInstance = Receiver.get(params.id)
        if (!receiverInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'receiver.label', default: 'Receiver'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [receiverInstance: receiverInstance]
        }
    }

    def update = {
        def receiverInstance = Receiver.get(params.id)
        if (receiverInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (receiverInstance.version > version) {
                    
                    receiverInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'receiver.label', default: 'Receiver')] as Object[], "Another user has updated this Receiver while you were editing")
                    render(view: "edit", model: [receiverInstance: receiverInstance])
                    return
                }
            }
            receiverInstance.properties = params
            if (!receiverInstance.hasErrors() && receiverInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'receiver.label', default: 'Receiver'), receiverInstance.id])}"
                redirect(action: "show", id: receiverInstance.id)
            }
            else {
                render(view: "edit", model: [receiverInstance: receiverInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'receiver.label', default: 'Receiver'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def receiverInstance = Receiver.get(params.id)
        if (receiverInstance) {
            try {
                receiverInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'receiver.label', default: 'Receiver'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'receiver.label', default: 'Receiver'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'receiver.label', default: 'Receiver'), params.id])}"
            redirect(action: "list")
        }
    }
}