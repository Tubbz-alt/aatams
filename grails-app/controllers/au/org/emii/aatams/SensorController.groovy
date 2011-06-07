package au.org.emii.aatams

class SensorController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [sensorInstanceList: Sensor.list(params), sensorInstanceTotal: Sensor.count()]
    }

    def create = {
        def sensorInstance = new Sensor()
        sensorInstance.properties = params
        return [sensorInstance: sensorInstance]
    }

    def save = {
        def sensorInstance = new Sensor(params)
        if (sensorInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'sensor.label', default: 'Sensor'), sensorInstance.id])}"
            redirect(action: "show", id: sensorInstance.id)
        }
        else {
            render(view: "create", model: [sensorInstance: sensorInstance])
        }
    }

    def show = {
        def sensorInstance = Sensor.get(params.id)
        if (!sensorInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'sensor.label', default: 'Sensor'), params.id])}"
            redirect(action: "list")
        }
        else {
            [sensorInstance: sensorInstance]
        }
    }

    def edit = {
        def sensorInstance = Sensor.get(params.id)
        if (!sensorInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'sensor.label', default: 'Sensor'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [sensorInstance: sensorInstance]
        }
    }

    def update = {
        def sensorInstance = Sensor.get(params.id)
        if (sensorInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (sensorInstance.version > version) {
                    
                    sensorInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'sensor.label', default: 'Sensor')] as Object[], "Another user has updated this Sensor while you were editing")
                    render(view: "edit", model: [sensorInstance: sensorInstance])
                    return
                }
            }
            sensorInstance.properties = params
            if (!sensorInstance.hasErrors() && sensorInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'sensor.label', default: 'Sensor'), sensorInstance.id])}"
                redirect(action: "show", id: sensorInstance.id)
            }
            else {
                render(view: "edit", model: [sensorInstance: sensorInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'sensor.label', default: 'Sensor'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def sensorInstance = Sensor.get(params.id)
        if (sensorInstance) {
            try {
                sensorInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'sensor.label', default: 'Sensor'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'sensor.label', default: 'Sensor'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'sensor.label', default: 'Sensor'), params.id])}"
            redirect(action: "list")
        }
    }
}